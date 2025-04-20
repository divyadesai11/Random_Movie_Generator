package ad.service;

import ad.api.IMDbAPIClient;
import ad.model.Movie;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MovieRecommendationService {
    // Cache for previously fetched movies
    private static final Map<String, Movie> movieCache = new ConcurrentHashMap<>();

    public List<Movie> getRecommendedMovies(String genre, String decade, String language) {
        System.out.println("Getting recommendations for: " + genre + ", " + decade + ", " + language);
        
        // Create search parameters for the API
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("genre", genre);
        searchParams.put("decade", decade);
        searchParams.put("language", language);
        
        // Get movie recommendations directly from the API
        List<String> imdbIds = IMDbAPIClient.searchMovies(searchParams);
        
        System.out.println("Found IMDb IDs: " + imdbIds);
        List<Movie> movies = new ArrayList<>();

        for (String imdbId : imdbIds) {
            // First check if movie is already in cache
            if (movieCache.containsKey(imdbId)) {
                System.out.println("Found movie in cache: " + imdbId);
                movies.add(movieCache.get(imdbId));
                continue;
            }
            
            System.out.println("Fetching details for IMDb ID: " + imdbId);
            String jsonResponse = IMDbAPIClient.getMovieDetails(imdbId);
            
            if (jsonResponse != null) {
                System.out.println("Received JSON response for: " + imdbId);
                Movie movie = parseMovieJson(jsonResponse);
                if (movie != null) {
                    System.out.println("Successfully parsed movie: " + movie.getTitle());
                    movieCache.put(imdbId, movie); // Cache the movie
                    movies.add(movie);
                }
            }
        }
        
        System.out.println("Returning " + movies.size() + " movies");
        return movies;
    }

    private Movie parseMovieJson(String json) {
        try {
            // Extract required fields from JSON based on the example response format
            String title = extractJsonValue(json, "title");
            
            // Extract description
            String description = extractJsonValue(json, "overview");
            
            // Extract rating - use IMDb rating if available
            String rating = extractJsonValue(json, "vote_average");
            if (rating.equals("N/A")) {
                rating = "Not Rated";
            }
            
            // Calculate duration - use runtime if available
            String runtimeMinutes = extractJsonValue(json, "runtime");
            if (runtimeMinutes.equals("N/A")) {
                // Try to extract release date for year information
                String releaseDate = extractJsonValue(json, "release_date");
                
                if (!releaseDate.equals("N/A")) {
                    runtimeMinutes = "Released: " + releaseDate;
                } else {
                    runtimeMinutes = "Duration unavailable";
                }
            } else {
                runtimeMinutes = runtimeMinutes + " mins";
            }
            
            String duration = runtimeMinutes;
            
            // Extract director from credits
            String director = extractDirectorFromCredits(json);
            
            // Extract cast members from credits
            String cast = extractCastFromCredits(json);
            
            // Extract poster URL
            String posterPath = extractJsonValue(json, "poster_path");
            String posterUrl = !posterPath.equals("N/A") ? 
                "https://image.tmdb.org/t/p/w500" + posterPath : "";
            
            return new Movie(
                title,
                rating,
                duration,
                director,
                cast,
                posterUrl,
                description
            );
        } catch (Exception e) {
            System.out.println("Error parsing movie JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String extractJsonValue(String json, String key) {
        String searchPattern = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchPattern);
        
        if (keyIndex == -1) return "N/A";
        
        int valueStart = keyIndex + searchPattern.length();
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= json.length()) return "N/A";
        
        char startChar = json.charAt(valueStart);
        
        if (startChar == '"') {
            // String value
            int valueEnd = findClosingQuote(json, valueStart + 1);
            if (valueEnd == -1) return "N/A";
            return unescapeJson(json.substring(valueStart + 1, valueEnd));
        } else if (startChar == '[') {
            // Array, return first few elements
            return extractArrayElements(json, valueStart);
        } else if (startChar == '{') {
            // Object
            return "Complex object";
        } else if (startChar == 'n' && json.substring(valueStart, Math.min(valueStart + 4, json.length())).equals("null")) {
            // Null value
            return "N/A";
        } else {
            // Number, boolean or other value
            int valueEnd = json.indexOf(',', valueStart);
            if (valueEnd == -1) {
                valueEnd = json.indexOf('}', valueStart);
            }
            if (valueEnd == -1) return "N/A";
            return json.substring(valueStart, valueEnd).trim();
        }
    }
    
    private String extractArrayElements(String json, int arrayStart) {
        // Find closing bracket for the array
        int openBrackets = 1;
        int closingBracket = arrayStart + 1;
        
        while (openBrackets > 0 && closingBracket < json.length()) {
            char c = json.charAt(closingBracket);
            if (c == '[') openBrackets++;
            else if (c == ']') openBrackets--;
            closingBracket++;
        }
        
        if (openBrackets > 0) return "Multiple items"; // Didn't find closing bracket
        
        // Extract array content
        String arrayContent = json.substring(arrayStart + 1, closingBracket - 1).trim();
        if (arrayContent.isEmpty()) return "None";
        
        // Check if it's an array of strings or objects
        if (arrayContent.charAt(0) == '"') {
            // Array of strings, extract values
            List<String> items = new ArrayList<>();
            int pos = 0;
            
            while (pos < arrayContent.length() && items.size() < 3) {
                int valueStart = arrayContent.indexOf('"', pos) + 1;
                if (valueStart <= 0) break;
                
                int valueEnd = arrayContent.indexOf('"', valueStart);
                if (valueEnd == -1) break;
                
                items.add(unescapeJson(arrayContent.substring(valueStart, valueEnd)));
                pos = valueEnd + 1;
            }
            
            return String.join(", ", items) + (items.size() < 3 ? "" : ", ...");
        } else if (arrayContent.charAt(0) == '{') {
            // Array of objects, extract a useful property if available
            int nameIndex = arrayContent.indexOf("\"name\":");
            
            if (nameIndex != -1) {
                List<String> names = new ArrayList<>();
                int pos = 0;
                
                while (nameIndex != -1 && names.size() < 3) {
                    int valueStart = arrayContent.indexOf('"', nameIndex + 7) + 1;
                    if (valueStart <= 0) break;
                    
                    int valueEnd = findClosingQuote(arrayContent, valueStart);
                    if (valueEnd == -1) break;
                    
                    names.add(unescapeJson(arrayContent.substring(valueStart, valueEnd)));
                    pos = valueEnd + 1;
                    nameIndex = arrayContent.indexOf("\"name\":", pos);
                }
                
                return String.join(", ", names) + (names.size() < 3 ? "" : ", ...");
            } else {
                return "Multiple items";
            }
        } else {
            return "Multiple items";
        }
    }
    
    private int findClosingQuote(String json, int start) {
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                return i;
            }
        }
        return -1;
    }
    
    private String unescapeJson(String value) {
        return value.replace("\\\"", "\"")
                   .replace("\\\\", "\\")
                   .replace("\\/", "/")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t");
    }
    
    private String extractDirectorFromCredits(String json) {
        // Look for the credits section, specifically the crew array
        int crewIndex = json.indexOf("\"crew\":");
        if (crewIndex == -1) return "Unknown Director";
        
        // Find the array start
        int arrayStart = json.indexOf('[', crewIndex);
        if (arrayStart == -1) return "Unknown Director";
        
        // Find the array end
        int arrayEnd = findClosingBracket(json, arrayStart);
        if (arrayEnd == -1) return "Unknown Director";
        
        // Extract the crew array content
        String crewArray = json.substring(arrayStart + 1, arrayEnd);
        
        // Look for directors in the crew array
        List<String> directors = new ArrayList<>();
        int pos = 0;
        
        while (pos < crewArray.length()) {
            // Find a crew member object
            int crewMemberStart = crewArray.indexOf("{", pos);
            if (crewMemberStart == -1) break;
            
            int crewMemberEnd = findClosingBrace(crewArray, crewMemberStart);
            if (crewMemberEnd == -1) break;
            
            String crewMember = crewArray.substring(crewMemberStart, crewMemberEnd + 1);
            
            // Check if this crew member is a director
            if (crewMember.contains("\"job\":\"Director\"")) {
                // Extract the name
                int nameIndex = crewMember.indexOf("\"name\":");
                if (nameIndex != -1) {
                    int nameStart = crewMember.indexOf('"', nameIndex + 7) + 1;
                    if (nameStart > 0) {
                        int nameEnd = findClosingQuote(crewMember, nameStart);
                        if (nameEnd != -1) {
                            directors.add(unescapeJson(crewMember.substring(nameStart, nameEnd)));
                        }
                    }
                }
            }
            
            pos = crewMemberEnd + 1;
        }
        
        if (!directors.isEmpty()) {
            return String.join(", ", directors);
        }
        
        // If no directors found, try to extract production companies as a fallback
        int productionIndex = json.indexOf("\"production_companies\":");
        if (productionIndex != -1) {
            int pcArrayStart = json.indexOf('[', productionIndex);
            if (pcArrayStart != -1) {
                String companies = extractArrayElements(json, pcArrayStart);
                return companies.equals("Multiple items") ? "Unknown Director" : "Production: " + companies;
            }
        }
        
        return "Unknown Director";
    }
    
    private String extractCastFromCredits(String json) {
        // Look for the cast array in the credits section
        int castIndex = json.indexOf("\"cast\":");
        if (castIndex == -1) return "Cast unavailable";
        
        // Find the array start
        int arrayStart = json.indexOf('[', castIndex);
        if (arrayStart == -1) return "Cast unavailable";
        
        // Find the array end
        int arrayEnd = findClosingBracket(json, arrayStart);
        if (arrayEnd == -1) return "Cast unavailable";
        
        // Extract the cast array content
        String castArray = json.substring(arrayStart + 1, arrayEnd);
        
        // Extract cast names
        List<String> castNames = new ArrayList<>();
        int pos = 0;
        
        while (pos < castArray.length() && castNames.size() < 5) {
            // Find a cast member object
            int castMemberStart = castArray.indexOf("{", pos);
            if (castMemberStart == -1) break;
            
            int castMemberEnd = findClosingBrace(castArray, castMemberStart);
            if (castMemberEnd == -1) break;
            
            String castMember = castArray.substring(castMemberStart, castMemberEnd + 1);
            
            // Extract the name
            int nameIndex = castMember.indexOf("\"name\":");
            if (nameIndex != -1) {
                int nameStart = castMember.indexOf('"', nameIndex + 7) + 1;
                if (nameStart > 0) {
                    int nameEnd = findClosingQuote(castMember, nameStart);
                    if (nameEnd != -1) {
                        castNames.add(unescapeJson(castMember.substring(nameStart, nameEnd)));
                    }
                }
            }
            
            pos = castMemberEnd + 1;
        }
        
        if (!castNames.isEmpty()) {
            return String.join(", ", castNames) + (castNames.size() >= 5 ? ", ..." : "");
        }
        
        // If no cast found, try to extract genres as fallback
        int genresIndex = json.indexOf("\"genres\":");
        if (genresIndex != -1) {
            int genresArrayStart = json.indexOf('[', genresIndex);
            if (genresArrayStart != -1) {
                String genres = extractArrayElements(json, genresArrayStart);
                return genres.equals("Multiple items") ? "Cast unavailable" : "Genres: " + genres;
            }
        }
        
        return "Cast unavailable";
    }
    
    private int findClosingBracket(String json, int start) {
        int openBrackets = 1;
        for (int i = start + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') openBrackets++;
            else if (c == ']') openBrackets--;
            
            if (openBrackets == 0) {
                return i;
            }
        }
        return -1;
    }
    
    private int findClosingBrace(String json, int start) {
        int openBraces = 1;
        for (int i = start + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') openBraces++;
            else if (c == '}') openBraces--;
            
            if (openBraces == 0) {
                return i;
            }
        }
        return -1;
    }
}