package sg.edu.nus.iss.miniproject.model;

public class Movie {
    
    private String title;
    private String year;
    private String type;
    private String imdbID;
    private String poster;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getImdbID() { return imdbID; }
    public void setImdbID(String imdbID) { this.imdbID = imdbID; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public Movie(String title, String year, String type, String imdbID, String poster) {
        this.title = title;
        this.year = year;
        this.type = type;
        this.imdbID = imdbID;
        this.poster = poster;
    }

    public Movie() {
    }

}
