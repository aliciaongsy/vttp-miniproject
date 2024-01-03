package sg.edu.nus.iss.miniproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.miniproject.service.MovieService;

@RestController
@RequestMapping
public class MovieDetailsController {

    @Autowired
    MovieService movieSvc;
    
    @GetMapping(path = "/details/json/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMovieDetails(@PathVariable String id){
        return movieSvc.getJsonDetails(id);
    }
}
