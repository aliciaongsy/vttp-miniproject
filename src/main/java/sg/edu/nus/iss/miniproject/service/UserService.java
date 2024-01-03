package sg.edu.nus.iss.miniproject.service;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import sg.edu.nus.iss.miniproject.model.Movie;
import sg.edu.nus.iss.miniproject.model.User;
import sg.edu.nus.iss.miniproject.repo.MovieRepo;

@Service
public class UserService {
    @Autowired
    MovieRepo movieRepo;

    // ---- user validation ----
    public boolean existingUser(String email) {
        return movieRepo.searchUserByEmail(email);
    }

    public boolean accountValidation(String email, String password) {
        // both pw and email correct
        if (movieRepo.searchUserByEmail(email) && movieRepo.checkPassword(email, password)) {
            return true;
        }
        // both pw and email wrong
        if (!movieRepo.searchUserByEmail(email) && !movieRepo.checkPassword(email, password)) {
            return false;
        }
        return true;
    }

    public void saveUser(User user) {
        movieRepo.addNewUser(user.getName(), user.getEmail(), user.getPassword(), user.getId(), user);
    }

    // ---- favourites list ----
    public List<Movie> createSavedList(List<String> list) {
        List<Movie> savedList = new LinkedList<>();
        for (int i = 0; i < list.size(); i++) {
            String moviedata = list.get(i);
            String[] array = moviedata.split("\\^");
            Movie movie = new Movie();
            movie.setPoster(array[0]);
            movie.setTitle(array[1]);
            movie.setYear(array[2]);
            movie.setType(array[3]);
            movie.setImdbID(array[4]);
            savedList.add(movie);
            System.out.println(movie.getTitle());
        }
        return savedList;
    }

    public JsonArray buildJsonArray(List<Movie> movies) {
        JsonArrayBuilder mainBuilder = Json.createArrayBuilder();
        for (Movie movie : movies) {
            // build individual java object
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("title", movie.getTitle())
                    .add("poster", movie.getPoster())
                    .add("year", movie.getYear())
                    .add("type", movie.getType())
                    .add("imdbID", movie.getImdbID());
            mainBuilder.add(builder);
        }
        // [ {indiv movie} {...} {...} ]
        JsonArray jsonArray = mainBuilder.build();
        return jsonArray;
    }

    public void saveFavourites(String email, List<String> data) {
        Optional<String> opt = movieRepo.getFavourites(email);

        System.out.println("Optional:" +opt.get()+"-");
        
        if (opt.isEmpty()) {
            System.out.println("Favourites list doesn't exist");
            JsonArray jsonArray = buildJsonArray(createSavedList(data));
            movieRepo.addFavourites(email, jsonArray);
        }

        if (opt.get().equals("[]")){
            System.out.println("Empty list");
            JsonArray jsonArray = buildJsonArray(createSavedList(data));
            movieRepo.addFavourites(email, jsonArray);
        }

        // ----- concat with current list -----
        // read from db
        else {
            System.out.println("Reading from db");
            JsonReader jsonReader = Json.createReader(new StringReader(opt.get()));
            JsonArray jsonArray = jsonReader.readArray();

            // newly added favourites
            JsonArray arr = buildJsonArray(createSavedList(data));

            // create a new array builder
            JsonArrayBuilder builder = Json.createArrayBuilder();

            // add jsonobject into the builder
            for (int i = 0; i < arr.size(); i++) {
                // prevent duplicate
                for (int j = 0; j < jsonArray.size(); j++) {
                    if (arr.getJsonObject(i).getString("title").equals(jsonArray.getJsonObject(j).getString("title"))) {
                        System.out.println("Repeated");
                        break;
                    }
                    if (j == jsonArray.size() - 1) {
                        System.out.println("Added");
                        System.out.println(arr.getJsonObject(i));
                        builder.add(arr.getJsonObject(i));
                        break;
                    }
                }
            }
            for (int i = 0; i < jsonArray.size(); i++) {
                System.out.println(jsonArray.getJsonObject(i));
                builder.add(jsonArray.getJsonObject(i));
            }

            JsonArray updatedArray = builder.build();
            movieRepo.addFavourites(email, updatedArray);
        }
    }

    public void delFavourites(String email, List<String> data) {
        Optional<String> opt = movieRepo.getFavourites(email);

        // retrieve imdb id
        String[] array = data.getFirst().split("\\^");
        String movieID = array[4];

        // read from db
        JsonReader jsonReader = Json.createReader(new StringReader(opt.get()));
        JsonArray jsonArray = jsonReader.readArray();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            // match using imdb ID
            if (jsonObject.getString("imdbID").equals(movieID)) {
                // remove json object from json array
                jsonArray = removeJsonObjectAtIndex(jsonArray, i);
                break;
            }
        }

        // update favourites list
        movieRepo.addFavourites(email, jsonArray);

    }

    private static JsonArray removeJsonObjectAtIndex(JsonArray jsonArray, int index) {
        // validate the index
        if (index < 0 || index >= jsonArray.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        JsonArrayBuilder builder = Json.createArrayBuilder();

        // add all elements before specified index
        for (int i = 0; i < index; i++) {
            builder.add(jsonArray.getJsonObject(i));
        }

        // add all elements after the specified index
        for (int i = index + 1; i < jsonArray.size(); i++) {
            builder.add(jsonArray.getJsonObject(i));
        }
        // create a new JsonArray with the specified JsonObject removed
        return builder.build();
    }

    public List<Movie> getFavourites(String email) {
        Optional<String> opt = movieRepo.getFavourites(email);

        List<Movie> favList = new LinkedList<>();
        if (opt.isEmpty()) {
            // return empty list
            return favList;
        }

        // read jsonarray that is stored in redis
        JsonReader jsonReader = Json.createReader(new StringReader(opt.get()));
        JsonArray jsonArray = jsonReader.readArray();
        favList = jsonArray.stream()
                .map(j -> j.asJsonObject())
                .map(o -> {
                    Movie movie = new Movie();
                    System.out.println(o);
                    movie.setTitle(o.getString("title"));
                    movie.setYear(o.getString("year"));
                    movie.setImdbID(o.getString("imdbID"));
                    movie.setType(o.getString("type"));
                    movie.setPoster(o.getString("poster"));
                    return movie;
                })
                .toList();

        return favList;
    }
}
