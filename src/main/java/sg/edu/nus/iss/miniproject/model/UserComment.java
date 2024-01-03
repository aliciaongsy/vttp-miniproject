package sg.edu.nus.iss.miniproject.model;

import java.util.Date;

public class UserComment {
    
    private String name;
    private String comment;
    private Date date;
    private int noOfLikes=0;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public int getNoOfLikes() { return noOfLikes; }
    public void setNoOfLikes(int noOfLikes) { this.noOfLikes = noOfLikes; }

    public UserComment(){
        
    }
    
}
