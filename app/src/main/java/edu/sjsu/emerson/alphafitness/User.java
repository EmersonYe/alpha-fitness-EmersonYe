package edu.sjsu.emerson.alphafitness;

/**
 * Created by emersonsjsu on 5/3/18.
 */

public class User
{
    private static User userInstance = null;
    private String name;
    private String gender;
    private String pictureUrl;
    private float weight;
    private float height;

    public User()
    {
    }

    public static User getInstance(){
        if(userInstance == null){
            userInstance = new User();
        }
        return userInstance;
    }
}
