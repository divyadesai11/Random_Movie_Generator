package ad.model;

/**
 * Model class for representing movie information retrieved from IMDb API.
 */
public class Movie {
    private String title;
    private String rating;
    private String duration;
    private String director;
    private String cast;
    private String posterUrl;
    private String description;

    /**
     * Constructor for creating a new Movie object.
     *
     * @param title The title of the movie
     * @param rating The IMDb rating of the movie
     * @param duration The duration of the movie in minutes
     * @param director The director of the movie
     * @param cast The cast members of the movie
     * @param posterUrl URL to the movie poster
     */
    public Movie(String title, String rating, String duration, String director, String cast, String posterUrl) {
        this.title = title != null ? title : "Unknown Title";
        this.rating = rating != null ? rating : "Not Rated";
        this.duration = duration != null ? duration : "Unknown Duration";
        this.director = director != null ? director : "Unknown Director";
        this.cast = cast != null ? cast : "Cast Information Unavailable";
        this.posterUrl = posterUrl != null ? posterUrl : "";
        this.description = "";
    }
    
    /**
     * Extended constructor including description
     */
    public Movie(String title, String rating, String duration, String director, String cast, String posterUrl, String description) {
        this(title, rating, duration, director, cast, posterUrl);
        this.description = description != null ? description : "";
    }

    /**
     * @return The title of the movie
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return The rating of the movie
     */
    public String getRating() {
        return rating;
    }

    /**
     * @return The duration of the movie in minutes
     */
    public String getDuration() {
        return duration;
    }

    /**
     * @return The director of the movie
     */
    public String getDirector() {
        return director;
    }

    /**
     * @return The cast members of the movie
     */
    public String getCast() {
        return cast;
    }

    /**
     * @return URL to the movie poster
     */
    public String getPosterUrl() {
        return posterUrl;
    }
    
    /**
     * @return Description of the movie
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the description of the movie
     */
    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", rating='" + rating + '\'' +
                ", duration='" + duration + '\'' +
                ", director='" + director + '\'' +
                ", cast='" + cast + '\'' +
                ", posterUrl='" + posterUrl + '\'' +
                '}';
    }
}