package ws.siri.proximity.backend;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

public class Records {
    /*
     * List of DiscordIDs that are subscribed to, used to build activePlayerNames
     */
    private static HashSet<String> activeDiscordIDs = new HashSet<>();

    /*
     * List of IGN of players who are currently in VC,
     * so information about them should be sent back to the Vencord plugin
     */
    private static HashSet<String> activePlayerNames = new HashSet<>();

    /*
     * Map<IGN, Discord ID>
     */
    private static HashMap<String, String> reverseTranslationTable = new HashMap<>();

    private static class CachedEntry {
        public final HashSet<String> igns;
        public final Instant lastfetch;

        public CachedEntry(HashSet<String> igns, Instant lastfetch) {
            this.igns = igns;
            this.lastfetch = lastfetch;
        }
    }

    /*
     * Cached responses
     * <ID, IGN>
     */
    private static HashMap<String, CachedEntry> fetchCache = new HashMap<>();

    /*
     * Build activePlayerNames from activeDiscordIDs
     */
    private static void buildActive(HashSet<String> activeIDs) {
        HashSet<String> newActivePlayerNames = new HashSet<>();

        for(String s : activeIDs) {
            if(fetchCache.containsKey(s)) {
                newActivePlayerNames.addAll(fetchCache.get(s).igns);
            }
        }

        activePlayerNames = newActivePlayerNames;
    }

    private static boolean fetchNow(Instant now, HashSet<String> shouldFetch) {
        if(shouldFetch.isEmpty()) return false;

        final String endpoint = "https://proximity.siri.ws/api/v1/whois";
        boolean updated = false;

        try {
            final HttpPost httpPost = new HttpPost(endpoint);
            final String json = "{\"ids\":["+shouldFetch.stream().map((name) -> "\"" + name + "\"").collect(Collectors.joining(","))+"]}";
            final StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            try (CloseableHttpClient client = HttpClients.createDefault();
                    CloseableHttpResponse response = (CloseableHttpResponse) client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if(statusCode > 299 || statusCode < 200) {
                    throw new RuntimeException("Fetch whois returned status code " + statusCode);
                }

                Map<?, ?> parsed = new Gson().fromJson(EntityUtils.toString(response.getEntity()), Map.class);

                for(Entry<?, ?> entry : ((Map<?, ?>) parsed).entrySet()) {
                    String discordID = (String) entry.getKey();
                    List<Object> igns = (List<Object>) entry.getValue();

                    HashSet<String> newIgns = new HashSet<>();

                    for(Object ign : igns) {
                        newIgns.add((String) ign);
                    }

                    if (fetchCache.containsKey(discordID)) {
                        HashSet<String> oldIgns = fetchCache.get(discordID).igns;

                        if(oldIgns.equals(newIgns)) {
                            continue;
                        }
                    }

                    updated = true;

                    fetchCache.put(discordID, new CachedEntry(newIgns, now));
                    for(String ign : newIgns) {
                        reverseTranslationTable.put(ign, discordID);
                    }
                }
                    }
        } catch (Exception e) {
            Logger.getLogger("ProximityBackend").log(Level.SEVERE, e.toString());
        }

        // igns added to fetchCache
        // also add entries to the reverse translation table
        //
        // returns true if something has CHANGED, not fetched
        // false otherwise
        return updated;
    }

    private static void subscribeFetch(String[] discordIDs) {
        // TODO: configurable
        final long retryCooldown = 120;
        final Instant now = Instant.now();

        // list of user IDs to fetch for
        HashSet<String> shouldFetch = new HashSet<>();

        for(String discordID : discordIDs) {
            if (fetchCache.containsKey(discordID)) {
                final Instant lastFetch = fetchCache.get(discordID).lastfetch;
                final Duration elapsed = Duration.between(lastFetch, now);

                if(elapsed.getSeconds() < retryCooldown) continue;
            }

            shouldFetch.add(discordID);
        }

        // only update build if something new has been fetched
        if(fetchNow(now, shouldFetch)) {
            buildActive(activeDiscordIDs);
        }
    }

    public static void unsubscribeAll() {
        activeDiscordIDs.clear();
        activePlayerNames.clear();
    }

    public static void unsubscribe(String[] unsubscribe) {
        for(String discordID : unsubscribe) {
            activeDiscordIDs.remove(discordID);
        }

        buildActive(activeDiscordIDs);
    }

    public static void subscribe(String[] subscribe) {
        for(String discordID : subscribe) {
            activeDiscordIDs.add(discordID);
        }
        buildActive(activeDiscordIDs);

        subscribeFetch(subscribe);
    }

    public static boolean playerIsActive(String name) {
        return activePlayerNames.contains(name);
    }

    private static HashMap<String, Double> distanceCache = new HashMap<>();
    private static HashMap<String, Double> volumeCache = new HashMap<>();

    private static final double cap = 1.3;
    private static final double scale = 1.3;
    private static final double force = 1.2;
    private static final double flattening = 2.0;

    public static double getVolume(String ign, double distance) {
        return Math.min(scale * flattening * Math.pow(distance + flattening, -force), cap);
    }

    public static boolean idInWorld(String id, HashSet<String> activePlayersInWorld) {
        return activePlayersInWorld.stream().anyMatch((ign) -> {
            return reverseTranslationTable.get(ign).equals(id);
        });
    }

    public static HashMap<String, Double> volumeDiff(HashMap<String, Double> old, HashMap<String, Double> current, HashSet<String> activePlayersInWorld) {
        HashMap<String, Double> diff = new HashMap<>();

        for(Entry<String, Double> oldEntry : old.entrySet()) {
            String id = oldEntry.getKey();

           if(!current.containsKey(id)) {
                diff.put(id, idInWorld(id, activePlayersInWorld) ? 0.0 : 1.0);
                continue;
            }

            Double oldValue = oldEntry.getValue();
            Double newValue = current.get(id);

            if(!oldValue.equals(newValue)) {
                diff.put(id, newValue);
            }
        }

        for(String ign : current.keySet()) {
            if(!old.containsKey(ign)) {
                diff.put(ign, current.get(ign));
            }
        }

        return diff;
    }

    /*
     * Takes: Map<IGN, distance>
     * Returns: Map<Discord ID, volume multiplier>
     */
    public static HashMap<String, Double> pushPlayers(HashMap<String, Double> distances, HashSet<String> activePlayersInWorld) {
        if(distances.equals(distanceCache)) {
            return new HashMap<>();
        } else {
            distanceCache = distances;
        }

        HashMap<String, Double> newVolumes = new HashMap<>();

        for(Entry<String, Double> entry : distances.entrySet()) {
            String ign = entry.getKey();
            Double distance = entry.getValue();

            if(!reverseTranslationTable.containsKey(entry.getKey())) continue;

            newVolumes.put(reverseTranslationTable.get(ign), getVolume(ign, distance));
        }

        HashMap<String, Double> diff = volumeDiff(volumeCache, newVolumes, activePlayersInWorld);
        volumeCache = newVolumes;

        return diff;
    }
}
