package edu.sjsu.emerson.alphafitness;

/**
 * Created by emersonsjsu on 5/3/18.
 */

public class User
{
    private static User userInstance = null;
    private String name;
    private String gender;
    private float weight;
    private float height;

    public User()
    {
        name = "Jon Doe";
        gender = "Male";
        weight = 950;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public float getWeight()
    {
        return weight;
    }

    public void setWeight(float weight)
    {
        this.weight = weight;
    }

    public float getHeight()
    {
        return height;
    }

    public void setHeight(float height)
    {
        this.height = height;
    }

    public static User getInstance(){
        if(userInstance == null){
            userInstance = new User();
        }
        return userInstance;
    }
}
