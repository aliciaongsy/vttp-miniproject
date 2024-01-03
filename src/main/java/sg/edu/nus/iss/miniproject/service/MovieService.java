package sg.edu.nus.iss.miniproject.service;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import sg.edu.nus.iss.miniproject.model.Movie;
import sg.edu.nus.iss.miniproject.model.MovieDetails;
import sg.edu.nus.iss.miniproject.model.UserComment;
import sg.edu.nus.iss.miniproject.repo.MovieRepo;

@Service
public class MovieService {

    @Autowired
    MovieRepo movieRepo;

    @Value("${movies.api.key}")
    private String movieApiKey;

    @Value("${netflix.api.key}")
    private String netflixApiKey;

    private String url = "https://www.omdbapi.com/";

    public List<Movie> searchMovies(String search, String type) throws UnsupportedEncodingException {

        String str = "%s-%s".formatted(search.toLowerCase(), type);
        Optional<String> opt = movieRepo.searchRedis(str);

        JsonArray jsonResult;

        // if data does not exist in db
        if (opt.isEmpty()) {

            String search_url;

            if (search.length() > 1) {
                String encodedSearchTerm = URLEncoder.encode(search, StandardCharsets.UTF_8.toString());
                search_url = UriComponentsBuilder
                        .fromUriString(url)
                        .queryParam("s", encodedSearchTerm)
                        .queryParam("type", type)
                        .queryParam("apikey", movieApiKey)
                        .toUriString();
            }

            else {
                // url to obtain search result
                search_url = UriComponentsBuilder
                        .fromUriString(url)
                        .queryParam("s", search)
                        .queryParam("type", type)
                        .queryParam("apikey", movieApiKey)
                        .toUriString();

                System.out.println(search_url);

            }

            RequestEntity<Void> req = RequestEntity.get(search_url).build();
            System.out.println(req);

            RestTemplate template = new RestTemplate();

            ResponseEntity<String> resp = template.exchange(req, String.class);
            System.out.println(resp);

            System.out.println(resp.getBody());
            System.out.println(resp.getStatusCode());

            // read json object
            JsonReader jsonReader = Json.createReader(new StringReader(resp.getBody()));
            JsonObject jsonObject = jsonReader.readObject();
            jsonResult = jsonObject.getJsonArray("Search");

            // check for empty search
            String Response = jsonObject.getString("Response");
            if (Response.equals("False")) {
                System.out.println("Empty search");
                List<Movie> list = new LinkedList<>();
                return list;
            }

            String totalResults = jsonObject.getString("totalResults");
            Integer count = Integer.parseInt(totalResults);
            Integer pageRequired = (int) Math.ceil(count / 10.0);

            // add first page of results
            JsonArrayBuilder builder = Json.createArrayBuilder();
            for (int j = 0; j < jsonResult.size(); j++) {
                builder.add(jsonResult.getJsonObject(j));
            }
            if (pageRequired >= 10) {
                for (int i = 2; i <= 10; i++) {
                    String search_url2;
                    if (search.length() > 1) {
                        String encodedSearchTerm = URLEncoder.encode(search, StandardCharsets.UTF_8.toString());
                        search_url2 = UriComponentsBuilder
                                .fromUriString(url)
                                .queryParam("s", encodedSearchTerm)
                                .queryParam("type", type)
                                .queryParam("page", i)
                                .queryParam("apikey", movieApiKey)
                                .toUriString();
                    }

                    else {
                        // url to obtain search result
                        search_url2 = UriComponentsBuilder
                                .fromUriString(url)
                                .queryParam("s", search)
                                .queryParam("type", type)
                                .queryParam("page", i)
                                .queryParam("apikey", movieApiKey)
                                .toUriString();

                        System.out.println(search_url2);

                    }
                    RequestEntity<Void> request = RequestEntity.get(search_url2).build();
                    
                    RestTemplate temp = new RestTemplate();

                    ResponseEntity<String> response = temp.exchange(request, String.class);

                    // read json object
                    JsonReader reader = Json.createReader(new StringReader(response.getBody()));
                    JsonObject object = reader.readObject();
                    jsonResult = object.getJsonArray("Search");
                    for (int j = 0; j < jsonResult.size(); j++) {
                        builder.add(jsonResult.getJsonObject(j));
                    }

                }
            }
            if (pageRequired >= 2 && pageRequired < 10) {
                for (int i = 2; i <= pageRequired; i++) {
                    String search_url2;
                    if (search.length() > 1) {
                        String encodedSearchTerm = URLEncoder.encode(search, StandardCharsets.UTF_8.toString());
                        search_url2 = UriComponentsBuilder
                                .fromUriString(url)
                                .queryParam("s", encodedSearchTerm)
                                .queryParam("type", type)
                                .queryParam("page", i)
                                .queryParam("apikey", movieApiKey)
                                .toUriString();
                    }

                    else {
                        // url to obtain search result
                        search_url2 = UriComponentsBuilder
                                .fromUriString(url)
                                .queryParam("s", search)
                                .queryParam("type", type)
                                .queryParam("page", i)
                                .queryParam("apikey", movieApiKey)
                                .toUriString();

                        System.out.println(search_url2);

                    }
                    System.out.println("Reading page " + i);

                    RequestEntity<Void> request = RequestEntity.get(search_url2).build();

                    RestTemplate temp = new RestTemplate();

                    ResponseEntity<String> response = temp.exchange(request, String.class);

                    // read json object
                    JsonReader reader = Json.createReader(new StringReader(response.getBody()));
                    JsonObject object = reader.readObject();
                    jsonResult = object.getJsonArray("Search");
                    for (int j = 0; j < jsonResult.size(); j++) {
                        builder.add(jsonResult.getJsonObject(j));
                    }
                }
            }
            jsonResult = builder.build();

            System.out.println("--------- saving data into redis ---------");
            movieRepo.cacheResult(str, jsonResult);
        }

        // if data exist in db
        else {
            System.out.println("--------- result from cache ---------");
            JsonReader jsonReader = Json.createReader(new StringReader(opt.get()));
            jsonResult = jsonReader.readArray();
        }

        List<Movie> list = jsonResult.stream()
                .map(j -> j.asJsonObject())
                .map(o -> {
                    Movie movie = new Movie();
                    movie.setTitle(o.getString("Title"));
                    movie.setYear(o.getString("Year"));
                    movie.setImdbID(o.getString("imdbID"));
                    movie.setType(o.getString("Type"));
                    movie.setPoster(o.getString("Poster"));
                    return movie;
                })
                .toList();

        return list;
    }

    // get details by id
    public MovieDetails getDetails(String id) throws ParseException {

        String search = id + "-details";
        Optional<String> opt = movieRepo.searchRedis(search);

        // if id does not exist in database
        if (opt.isEmpty()) {
            String details_url = UriComponentsBuilder
                    .fromUriString(url)
                    .queryParam("i", id)
                    .queryParam("plot", "full")
                    .queryParam("apikey", movieApiKey)
                    .toUriString();

            RequestEntity<Void> req = RequestEntity.get(details_url).build();

            RestTemplate template = new RestTemplate();

            ResponseEntity<String> resp = template.exchange(req, String.class);

            JsonReader jsonReader = Json.createReader(new StringReader(resp.getBody()));
            JsonObject jsonObject = jsonReader.readObject();

            MovieDetails movieDetails = new MovieDetails();
            movieDetails.setTitle(jsonObject.getString("Title"));
            movieDetails.setYear(jsonObject.getString("Year"));
            movieDetails.setRated(jsonObject.getString("Rated"));
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            movieDetails.setReleased(formatter.parse(jsonObject.getString("Released")));
            movieDetails.setRuntime(jsonObject.getString("Runtime"));
            movieDetails.setGenre(jsonObject.getString("Genre"));
            movieDetails.setPlot(jsonObject.getString("Plot"));
            movieDetails.setPoster(jsonObject.getString("Poster"));
            movieDetails.setActors(jsonObject.getString("Actors"));
            movieDetails.setImdbRating(Float.parseFloat(jsonObject.getString("imdbRating")));
            movieDetails.setImdbID(jsonObject.getString("imdbID"));
            movieDetails.setType(jsonObject.getString("Type"));
            if (searchNetflix(jsonObject.getString("imdbID")) == 200) {
                movieDetails.setOnNetflix(true);
            }

            JsonArrayBuilder builder = Json.createArrayBuilder();
            builder.add(jsonObject);
            JsonArray jsonArray = builder.build();

            // save to redis db
            System.out.println("--------- saving data into redis ---------");
            movieRepo.cacheResult(search, jsonArray);

            return movieDetails;
        }

        // id exist in db
        else {
            System.out.println("--------- result from cache ---------");
            JsonReader jsonReader = Json.createReader(new StringReader(opt.get()));
            JsonArray jsonArray = jsonReader.readArray();
            JsonObject jsonObject = jsonArray.getJsonObject(0);

            MovieDetails movieDetails = new MovieDetails();
            movieDetails.setTitle(jsonObject.getString("Title"));
            movieDetails.setYear(jsonObject.getString("Year"));
            movieDetails.setRated(jsonObject.getString("Rated"));
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            movieDetails.setReleased(formatter.parse(jsonObject.getString("Released")));
            movieDetails.setRuntime(jsonObject.getString("Runtime"));
            movieDetails.setGenre(jsonObject.getString("Genre"));
            movieDetails.setPlot(jsonObject.getString("Plot"));
            movieDetails.setPoster(jsonObject.getString("Poster"));
            movieDetails.setActors(jsonObject.getString("Actors"));
            movieDetails.setImdbRating(Float.parseFloat(jsonObject.getString("imdbRating")));
            movieDetails.setImdbID(jsonObject.getString("imdbID"));
            movieDetails.setType(jsonObject.getString("Type"));
            if (searchNetflix(jsonObject.getString("imdbID")) == 200) {
                movieDetails.setOnNetflix(true);
            }
            return movieDetails;
        }

    }

    // get ratings
    public ResponseEntity<String> getJsonDetails(String id) {
        String search = id + "-details";
        Optional<String> opt = movieRepo.searchRedis(search);

        // if id does not exist in database
        if (opt.isEmpty()) {
            String details_url = UriComponentsBuilder
                    .fromUriString(url)
                    .queryParam("i", id)
                    .queryParam("plot", "full")
                    .queryParam("apikey", movieApiKey)
                    .toUriString();

            RequestEntity<Void> req = RequestEntity.get(details_url).build();

            RestTemplate template = new RestTemplate();

            ResponseEntity<String> resp = template.exchange(req, String.class);

            JsonReader jsonReader = Json.createReader(new StringReader(resp.getBody()));
            JsonArray jsonArray = jsonReader.readArray();
            JsonObject jsonObject = jsonArray.getJsonObject(0);
            JsonArray rating = jsonObject.getJsonArray("Ratings");

            return ResponseEntity.ok().body(rating.toString());
        }

        else {

            JsonReader jsonReader = Json.createReader(new StringReader(opt.get()));
            JsonArray jsonArray = jsonReader.readArray();
            JsonObject jsonObject = jsonArray.getJsonObject(0);
            JsonArray rating = jsonObject.getJsonArray("Ratings");
            return ResponseEntity.ok().body(rating.toString());
        }

    }

    // check if netflix streaming is available
    public Integer searchNetflix(String id) {

        String str = id + "-netflixstatus";
        Optional<String> opt = movieRepo.searchRedis(str);

        if (opt.isEmpty()) {
            String search_url = UriComponentsBuilder
                    .fromUriString("https://unogsng.p.rapidapi.com/title")
                    .queryParam("imdbid", id)
                    .toUriString();

            RequestEntity<Void> req = RequestEntity.get(search_url)
                    .header("X-RapidAPI-Key", netflixApiKey)
                    .header("X-RapidAPI-Host", "unogsng.p.rapidapi.com")
                    .build();

            RestTemplate template = new RestTemplate();

            ResponseEntity<String> resp = template.exchange(req, String.class);

            Integer statuscode = resp.getStatusCode().value();

            movieRepo.cacheResult(id, statuscode);

            return statuscode;

        } else {
            Integer statuscode = Integer.parseInt(opt.get());
            return statuscode;
        }

    }

    public void addComments(String imdbID, String name, String comments) {
        movieRepo.addComments(imdbID, name, comments);
    }

    public List<UserComment> retrieveComments(String imdbID, String comments) throws ParseException {
        List<UserComment> commentslist = movieRepo.getComments(imdbID);

        if (commentslist.isEmpty()) {
            return new LinkedList<>();
        }

        return commentslist;
    }

    public void updateLikes(String imdbID, String name) {
        movieRepo.updateLikes(imdbID, name);
    }

}
