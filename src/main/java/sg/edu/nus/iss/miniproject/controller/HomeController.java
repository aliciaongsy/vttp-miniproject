package sg.edu.nus.iss.miniproject.controller;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import sg.edu.nus.iss.miniproject.model.User;
import sg.edu.nus.iss.miniproject.repo.MovieRepo;
import sg.edu.nus.iss.miniproject.service.MovieService;
import sg.edu.nus.iss.miniproject.service.UserService;

@Controller
@RequestMapping
public class HomeController {

    @Autowired
    MovieService movieSvc;

    @Autowired
    UserService userSvc;

    @Autowired
    MovieRepo movieRepo;

    private boolean loggedIn = false;
    private boolean emptySearch = false;
    private int searchCount = 0;

    // home page
    @GetMapping(path = "/home")
    public String search(Model model, HttpSession sess) {
        model.addAttribute("loggedin", loggedIn);

        if (loggedIn) {
            model.addAttribute("username", (String) sess.getAttribute("user"));
            model.addAttribute("id", (String) sess.getAttribute("id"));

        }

        // for displaying empty field error
        if (searchCount >= 1) {
            emptySearch = true;
            model.addAttribute("emptysearch", emptySearch);
        }

        model.addAttribute("emptysearch", emptySearch);

        return "home";
    }

    // recommendation page
    @GetMapping(path = "/recommendation")
    public String displayRecco(Model model, HttpSession sess) throws ParseException {
        model.addAttribute("loggedin", loggedIn);

        if (loggedIn) {
            model.addAttribute("username", (String) sess.getAttribute("user"));
            model.addAttribute("id", (String) sess.getAttribute("id"));

        }
        // add classic movies
        model.addAttribute("thenotebook", movieSvc.getDetails("tt0332280"));
        model.addAttribute("diehard", movieSvc.getDetails("tt0095016"));
        model.addAttribute("eternalsunshine", movieSvc.getDetails("tt0338013"));
        model.addAttribute("homealone", movieSvc.getDetails("tt0099785"));
        model.addAttribute("forrest", movieSvc.getDetails("tt0109830"));
        model.addAttribute("prideprejudice", movieSvc.getDetails("tt0414387"));
        model.addAttribute("godfather", movieSvc.getDetails("tt0068646"));
        model.addAttribute("rocky", movieSvc.getDetails("tt0075148"));
        model.addAttribute("beautifulmind", movieSvc.getDetails("tt0268978"));

        // add inspirational movies
        model.addAttribute("happyness", movieSvc.getDetails("tt0454921"));
        model.addAttribute("blindside", movieSvc.getDetails("tt0878804"));
        model.addAttribute("lifeofpi", movieSvc.getDetails("tt0454876"));
        model.addAttribute("goodwillhunting", movieSvc.getDetails("tt0119217"));

        // add disney
        model.addAttribute("walle", movieSvc.getDetails("tt0910970"));
        model.addAttribute("snowwhite", movieSvc.getDetails("tt0029583"));
        model.addAttribute("lionking", movieSvc.getDetails("tt0110357"));
        model.addAttribute("up", movieSvc.getDetails("tt1049413"));
        model.addAttribute("toystory", movieSvc.getDetails("tt0114709"));
        model.addAttribute("ratatouille", movieSvc.getDetails("tt0382932"));

        return "recommendation";
    }

    // results page
    @GetMapping(path = "/result")
    public String displayResult(@RequestParam MultiValueMap<String, String> form, Model model, HttpSession sess) throws UnsupportedEncodingException {

        String search = form.getFirst("show");
        String type = form.getFirst("type");
        sess.setAttribute("search", search);
        sess.setAttribute("type", type);

        // checking for empty search field
        if (search.isEmpty()) {
            searchCount += 1;
            System.out.println(searchCount);
            return "redirect:/home";
        }
        searchCount = 0;
        emptySearch = false;

        model.addAttribute("search", search);
        model.addAttribute("movies", movieSvc.searchMovies(search, type));
        model.addAttribute("loggedin", loggedIn);

        if (loggedIn) {
            model.addAttribute("username", (String) sess.getAttribute("user"));
            model.addAttribute("id", (String) sess.getAttribute("id"));
        }
        return "result";
    }

    // add to favourites button
    @PostMapping(path = "/result/favourites")
    public String addToFavourites(@RequestBody MultiValueMap<String, String> form, HttpSession sess, Model model) {
        // retrieve list of movie details in string
        List<String> data = form.get("favourite");
        System.out.println(data);
        // save to redis database
        userSvc.saveFavourites((String) sess.getAttribute("email"), data);

        String search = (String) sess.getAttribute("search");
        String type = (String) sess.getAttribute("type");

        return "redirect:/result?type=" + type + "&show=" + search;
    }

    // movie details page
    @GetMapping(path = "/details/{imdbid}")
    public String getDetails(@PathVariable String imdbid, Model model, HttpSession sess) throws ParseException {
        model.addAttribute("moviedetail", movieSvc.getDetails(imdbid));
        model.addAttribute("id", sess.getAttribute("id"));

        model.addAttribute("loggedin", loggedIn);
        if (loggedIn) {
            model.addAttribute("username", (String) sess.getAttribute("user"));
            model.addAttribute("id", (String) sess.getAttribute("id"));
        }

        // retrieve comments from db
        model.addAttribute("comments", movieRepo.getComments(imdbid));

        return "moviedetails";
    }

    // add to favourites button
    @PostMapping(path = "/details/favourites")
    public String addToFav(@RequestBody MultiValueMap<String, String> form, HttpSession sess, Model model) {
        // retrieve list of movie details in string
        List<String> data = form.get("favourite");
        String movieid = form.getFirst("movieid");
        System.out.println(data);
        // save to redis database
        userSvc.saveFavourites((String) sess.getAttribute("email"), data);

        return "redirect:/details/" + movieid;
    }

    @PostMapping(path = "/details/comments")
    public String addComments(@RequestBody MultiValueMap<String, String> form, HttpSession sess) {
        String movieid = form.getFirst("movieid");
        String comments = form.getFirst("comment");

        // save comments to db
        movieSvc.addComments(movieid, (String) sess.getAttribute("user"), comments);

        return "redirect:/details/" + movieid;
    }

    @PostMapping(path = "/details/comments/likes")
    public String increaseLikes(@RequestBody MultiValueMap<String, String> form, HttpSession sess) {
        String movieid = form.getFirst("movieid");

        // update likes in db
        movieSvc.updateLikes(movieid, (String) sess.getAttribute("user"));

        return "redirect:/details/" + movieid;
    }

    // login page
    @GetMapping(path = "/login")
    public String login(Model model) {
        model.addAttribute("validemail", true);
        model.addAttribute("validpw", true);
        model.addAttribute("field", true);
        return "login";
    }

    // check if account exist in database
    @PostMapping(path = "/login/validate", consumes = "application/x-www-form-urlencoded")
    public String validateUser(@ModelAttribute("user") User user, Model model, HttpSession sess) {

        // check for empty fields
        if (user.getEmail() == null || user.getPassword() == null) {
            model.addAttribute("field", false);
            model.addAttribute("validemail", true);
            model.addAttribute("validpw", true);
            return "login";
        }

        // check if user exist in db
        if (!movieRepo.searchUserByEmail(user.getEmail())) {
            System.out.println("Invalid email");
            model.addAttribute("field", true);
            model.addAttribute("validemail", false);
            model.addAttribute("validpw", true);
            return "login";
        }

        // check if password is correct
        if (!movieRepo.checkPassword(user.getEmail(), user.getPassword())) {
            System.out.println("Invalid password");
            model.addAttribute("field", true);
            model.addAttribute("validemail", true);
            model.addAttribute("validpw", false);
            return "login";
        }

        sess.setAttribute("user", movieRepo.retrieveUsername(user.getEmail()));
        sess.setAttribute("email", user.getEmail());
        sess.setAttribute("id", movieRepo.findIdByEmail(user.getEmail()));

        loggedIn = true;
        return "redirect:/home";
    }

    // sign up page
    @GetMapping(path = "/register")
    public String registerNewUser(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("exist", false);
        model.addAttribute("match", true);
        return "register";
    }

    // validation for creating new user
    @PostMapping(path = "/register/validate", consumes = "application/x-www-form-urlencoded")
    public String addNewUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {

        // check if user already exist
        if (userSvc.existingUser(user.getEmail())) {
            System.out.println("Existing email error");
            model.addAttribute("exist", true);
            model.addAttribute("match", true);
            return "register";
        }

        // validation check
        if (result.hasErrors()) {
            System.out.println("Validation error");

            model.addAttribute("exist", false);
            model.addAttribute("match", true);
            return "register";
        }

        // check if password match
        if (!(user.getConfirmPassword().equals(user.getPassword()))) {
            System.out.println("Password error");
            model.addAttribute("match", false);
            return "register";
        }

        else {
            // save user info into db
            userSvc.saveUser(user);
            return "success";
        }

    }

    // signout button
    @GetMapping(path = "/signout")
    public String signout(HttpSession sess) {
        // clear data
        loggedIn = false;
        sess.invalidate();
        return "redirect:/home";
    }

    // favourites page
    @GetMapping(path = "/{id}/favourites")
    public String getFavourites(@PathVariable String id, Model model, HttpSession sess) {
        model.addAttribute("loggedin", loggedIn);
        model.addAttribute("username", sess.getAttribute("user"));
        model.addAttribute("id", sess.getAttribute("id"));
        model.addAttribute("favlist", userSvc.getFavourites((String) sess.getAttribute("email")));
        return "favourites";
    }

    // remove favourites from list
    @PostMapping(path = "/{id}/favourites/remove")
    public String removeFav(@RequestBody MultiValueMap<String, String> form, HttpSession sess) {

        List<String> data = form.get("favourite");
        // String movieid = form.getFirst("movieid");

        userSvc.delFavourites((String) sess.getAttribute("email"), data);

        String id = (String) sess.getAttribute("id");

        return "redirect:/" + id + "/favourites";
    }
}
