package ad.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client to interact with the IMDb API for movie data
 */
public class IMDbAPIClient {
    // API Base URLs
    private static final String SEARCH_BASE_URL = "https://api.themoviedb.org/3/discover/movie";
    private static final String DETAILS_BASE_URL = "https://api.themoviedb.org/3/movie/";
    
    // Your API key - in production, this should be stored securely
    private static final String API_KEY = "3fd2be6f0c70a2a598f084ddfb75487c";
    
    // Genre mapping between friendly names and TMDB IDs
    private static final Map<String, Integer> GENRE_MAP = new HashMap<>();
    static {
        GENRE_MAP.put("Action", 28);
        GENRE_MAP.put("Adventure", 12);
        GENRE_MAP.put("Animation", 16);
        GENRE_MAP.put("Comedy", 35);
        GENRE_MAP.put("Crime", 80);
        GENRE_MAP.put("Documentary", 99);
        GENRE_MAP.put("Drama", 18);
        GENRE_MAP.put("Family", 10751);
        GENRE_MAP.put("Fantasy", 14);
        GENRE_MAP.put("History", 36);
        GENRE_MAP.put("Horror", 27);
        GENRE_MAP.put("Music", 10402);
        GENRE_MAP.put("Mystery", 9648);
        GENRE_MAP.put("Romance", 10749);
        GENRE_MAP.put("Sci-Fi", 878);
        GENRE_MAP.put("Science Fiction", 878);
        GENRE_MAP.put("TV Movie", 10770);
        GENRE_MAP.put("Thriller", 53);
        GENRE_MAP.put("War", 10752);
        GENRE_MAP.put("Western", 37);
    }
   
    // Language code mapping
    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>();
    static {
        LANGUAGE_MAP.put("English", "en");
        LANGUAGE_MAP.put("Hindi", "hi");
        LANGUAGE_MAP.put("Spanish", "es");
        LANGUAGE_MAP.put("French", "fr");
        LANGUAGE_MAP.put("German", "de");
        LANGUAGE_MAP.put("Italian", "it");
        LANGUAGE_MAP.put("Japanese", "ja");
        LANGUAGE_MAP.put("Korean", "ko");
        LANGUAGE_MAP.put("Chinese", "zh");
        LANGUAGE_MAP.put("Russian", "ru");
        LANGUAGE_MAP.put("Portuguese", "pt");
        LANGUAGE_MAP.put("Arabic", "ar");
        LANGUAGE_MAP.put("Turkish", "tr");
    }
    
    /**
     * Search for movies based on given parameters
     * @param params Map containing search parameters
     * @return List of IMDb IDs for matching movies
     */
    public static List<String> searchMovies(Map<String, String> params) {
        List<String> movieIds = new ArrayList<>();
        
        try {
            StringBuilder urlBuilder = new StringBuilder(SEARCH_BASE_URL);
            urlBuilder.append("?api_key=").append(API_KEY)
                     .append("&sort_by=popularity.desc")
                     .append("&include_adult=false");
            
            // Process genre
            String genre = params.get("genre");
            if (genre != null && !genre.isEmpty() && GENRE_MAP.containsKey(genre)) {
                urlBuilder.append("&with_genres=").append(GENRE_MAP.get(genre));
            }
            
            // Process decade
            String decade = params.get("decade");
            if (decade != null && !decade.isEmpty()) {
                try {
                    int startYear = Integer.parseInt(decade);
                    int endYear = startYear + 9;
                    urlBuilder.append("&primary_release_date.gte=").append(startYear).append("-01-01");
                    urlBuilder.append("&primary_release_date.lte=").append(endYear).append("-12-31");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid decade format: " + decade);
                }
            }
            
            // Process language
            String language = params.get("language");
            if (language != null && !language.isEmpty() && LANGUAGE_MAP.containsKey(language)) {
                urlBuilder.append("&with_original_language=").append(LANGUAGE_MAP.get(language));
            }
            
            
            System.out.println("Search URL: " + urlBuilder.toString());
            
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Parse JSON response to extract movie IDs
                // Format: {"results":[{"id":123,"title":"Movie Title",...},{...}]}
                String jsonResponse = response.toString();
                
                int resultsStartIndex = jsonResponse.indexOf("\"results\":[");
                if (resultsStartIndex != -1) {
                    int startIndex = resultsStartIndex + 11; // Length of "\"results\":["
                    
                    // Find end of results array
                    int endIndex = findClosingBracket(jsonResponse, startIndex);
                    if (endIndex != -1) {
                        String resultsArray = jsonResponse.substring(startIndex, endIndex);
                        
                        // Parse each movie object in the results array
                        int currentPos = 0;
                        while (currentPos < resultsArray.length()) {
                            int movieObjStart = resultsArray.indexOf("{", currentPos);
                            if (movieObjStart == -1) break;
                            
                            int movieObjEnd = findClosingBrace(resultsArray, movieObjStart);
                            if (movieObjEnd == -1) break;
                            
                            String movieObj = resultsArray.substring(movieObjStart, movieObjEnd + 1);
                            
                            // Extract movie ID
                            int idIndex = movieObj.indexOf("\"id\":");
                            if (idIndex != -1) {
                                idIndex += 5; // Length of "\"id\":"
                                int idEndIndex = movieObj.indexOf(",", idIndex);
                                if (idEndIndex == -1) {
                                    idEndIndex = movieObj.indexOf("}", idIndex);
                                }
                                
                                if (idEndIndex != -1) {
                                    String idStr = movieObj.substring(idIndex, idEndIndex).trim();
                                    movieIds.add(idStr);
                                    
                                    // Limit to 10 movies
                                    if (movieIds.size() >= 10) {
                                        break;
                                    }
                                }
                            }
                            
                            currentPos = movieObjEnd + 1;
                        }
                    }
                }
            } else {
                System.out.println("Error response code: " + responseCode);
            }
            
        } catch (Exception e) {
            System.out.println("Error searching movies: " + e.getMessage());
            e.printStackTrace();
        }
        
        return movieIds;
    }
    
    /**
     * Get detailed information for a specific movie by ID
     * @param movieId The IMDb ID of the movie
     * @return JSON string containing movie details
     */
    public static String getMovieDetails(String movieId) {
        try {
            URL url = new URL(DETAILS_BASE_URL + movieId + "?api_key=" + API_KEY + "&append_to_response=credits,keywords");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                return response.toString();
            } else {
                System.out.println("Error response code for movie details: " + responseCode);
            }
            
        } catch (Exception e) {
            System.out.println("Error getting movie details: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Helper method to find the closing bracket in a JSON array
     */
    private static int findClosingBracket(String json, int startIndex) {
        int openBrackets = 1;
        for (int i = startIndex; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') {
                openBrackets++;
            } else if (c == ']') {
                openBrackets--;
                if (openBrackets == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Helper method to find the closing brace in a JSON object
     */
    private static int findClosingBrace(String json, int startIndex) {
        int openBraces = 1;
        for (int i = startIndex + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                openBraces++;
            } else if (c == '}') {
                openBraces--;
                if (openBraces == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}