package sg.edu.nus.iss.miniproject.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieDetails {

    private String Title;
    private String Year;
    private String Rated;
    private Date Released;
    private String Runtime;
    private String Genre;
    private String Actors;
    private String Plot;
    private String Poster;
    private Float imdbRating;
    private String imdbID;
    private String Type;
    private boolean onNetflix;

    public String getTitle() { return Title; }
    public void setTitle(String title) { Title = title; }
   
    public String getYear() { return Year; }
    public void setYear(String year) { Year = year; }

    public String getRated() { return Rated; }
    public void setRated(String rated) {  Rated = rated; }

    public Date getReleased() { return Released; }
    public void setReleased(Date released) { Released = released; }
    
    public String getRuntime() { return Runtime; }
    public void setRuntime(String runtime) { Runtime = runtime; }

    public String getGenre() { return Genre; }
    public void setGenre(String genre) { Genre = genre; }

    public String getActors() { return Actors; }
    public void setActors(String actors) { Actors = actors; }

    public String getPlot() { return Plot; }
    public void setPlot(String plot) { Plot = plot; }

    public String getPoster() { return Poster; }
    public void setPoster(String poster) { Poster = poster; }

    public Float getImdbRating() { return imdbRating; }
    public void setImdbRating(Float imdbRating) { this.imdbRating = imdbRating; }

    public String getImdbID() { return imdbID; }
    public void setImdbID(String imdbID) { this.imdbID = imdbID; } 

    public String getType() { return Type; }
    public void setType(String type) { Type = type; }

    public boolean isOnNetflix() { return onNetflix; }
    public void setOnNetflix(boolean onNetflix) { this.onNetflix = onNetflix; }

    public MovieDetails(){

    }
}
