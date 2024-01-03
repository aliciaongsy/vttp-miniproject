package sg.edu.nus.iss.miniproject.repo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.json.JsonArray;
import sg.edu.nus.iss.miniproject.PassBasedEnc;
import sg.edu.nus.iss.miniproject.model.User;
import sg.edu.nus.iss.miniproject.model.UserComment;

@Repository
public class MovieRepo {

    @Value("${moviesapi.cache.timeout.days}")
    long timeout;

    @Autowired
    @Qualifier("myredis")
    private RedisTemplate<String, String> template;

    private List<User> userList = new LinkedList<>();

    // search for search result in redis db
    public Optional<String> searchRedis(String searchKey) {
        return Optional.ofNullable(template.opsForValue().get(searchKey));
    }

    // cache result for movies result
    public void cacheResult(String searchKey, JsonArray jsonArray) {
        // clear db everyday
        template.opsForValue().set(searchKey, jsonArray.toString(), timeout, TimeUnit.DAYS);
    }

    // cache result for netflix status
    public void cacheResult(String imdbID, Integer status) {
        String str = imdbID + "-netflixstatus";
        template.opsForValue().set(str, status.toString());
    }

    public void addNewUser(String name, String email, String password, String id, User user) {
        userList.add(user);
        template.opsForHash().put(email, "name", name);
        template.opsForHash().put(email, "id", id);

        /* generates the Salt value. It can be stored in a database. */
        String saltvalue = PassBasedEnc.getSaltvalue(30);

        /* generates an encrypted password. It can be stored in a database. */
        String encryptedpassword = PassBasedEnc.generateSecurePassword(password, saltvalue);

        /* Print out plain text password, encrypted password and salt value. */
        // System.out.println("Plain text password = " + password);
        // System.out.println("Secure password = " + encryptedpassword);
        // System.out.println("Salt value = " + saltvalue);

        template.opsForHash().put(email, "password", encryptedpassword);
        template.opsForHash().put(email, "salt", saltvalue);
    }

    public String findIdByEmail(String email) {
        return userList.stream().filter(s -> s.getEmail().equals(email)).findFirst().get().getId();
    }

    public boolean searchUserByEmail(String email) {
        if (template.opsForHash().keys(email).isEmpty()) {
            // false if user doesn't exist in db
            return false;
        }
        // true if user exist in db
        return true;
    }

    // check if password keyed matches the password in db
    public boolean checkPassword(String email, String password) {
        Optional<Object> encryptedpassword = Optional.ofNullable(template.opsForHash().get(email, "password"));
        Optional<Object> saltvalue = Optional.ofNullable(template.opsForHash().get(email, "salt"));

        if (encryptedpassword.isEmpty() && saltvalue.isEmpty()) {
            return false;
        }
        /* verify the original password and encrypted password */
        Boolean status = PassBasedEnc.verifyUserPassword(password, (String) encryptedpassword.get(),
                (String) saltvalue.get());
        if (status == true) {
            System.out.println("Password Matched!!");
            return true;
        }
        System.out.println("Password Mismatched");
        return false;
    }

    public String retrieveUsername(String email) {
        return template.opsForHash().get(email, "name").toString();
    }

    public void addFavourites(String email, JsonArray jsonArray) {
        template.opsForHash().put(email, "favourites", jsonArray.toString());
    }

    public Optional<String> getFavourites(String email) {
        return Optional.ofNullable((String) template.opsForHash().get(email, "favourites"));
    }

    // get all the current user from database and add to list
    public void getUserDB() {
        Set<String> allKeys = template.keys("*");
        List<String> keys = allKeys.stream().filter(k -> k.contains("@")).toList();

        for (String key : keys) {
            User user = new User();
            user.setEmail(key);
            user.setId((String) template.opsForHash().get(key, "id"));
            user.setName((String) template.opsForHash().get(key, "name"));
            user.setName((String) template.opsForHash().get(key, "password"));
            userList.add(user);
        }
    }

    public void addComments(String imdbID, String name, String comments) {
        String str = imdbID + "-comments";

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        // name,comments,date
        // initialise number of likes to 0
        String data = "%s,%s,%s,%d".formatted(name, comments, formatter.format(date), 0);

        template.opsForList().leftPush(str, data);
    }

    public List<UserComment> getComments(String imdbID) throws ParseException {
        String str = imdbID + "-comments";
        List<UserComment> commentslist = new LinkedList<>();

        // retrieving data from redis db
        // range(key, start index, end index)
        for (String i : template.opsForList().range(str, 0, -1)) {
            String[] terms = i.split(",");
            UserComment usercomment = new UserComment();
            usercomment.setName(terms[0]);
            usercomment.setComment(terms[1]);
            String dateInString = terms[2];
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            usercomment.setDate(formatter.parse(dateInString));
            usercomment.setNoOfLikes(Integer.parseInt(terms[3]));
            commentslist.add(usercomment);
        }
        return commentslist;
    }

    // everytime this function is called, number of likes increase by 1
    public void updateLikes(String imdbID, String name) {
        String str = imdbID + "-comments";

        // retrieving data from redis db
        // range(key, start index, end index)
        for (String i : template.opsForList().range(str, 0, -1)) {
            Long index = template.opsForList().indexOf(str, i);
            String[] terms = i.split(",");
            // identify user
            if (terms[0].equals(name)) {               
                String data = "%s,%s,%s,%d".formatted(name, terms[1], terms[2], Integer.parseInt(terms[3])+1);
                template.opsForList().set(str, index, data);
            }
        }
    }
}
