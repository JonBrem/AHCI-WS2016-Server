package meme_recommender.recommender_engine;

import de.ur.ahci.model.Meme;

public class MemeRecommender {

    public MemeRecommender() {

    }

    /**
     *
     * @param userId
     * id of the user that gets the recommendation
     * @param howManyMemes
     * @return
     * Array of recommended Memes
     */
    public Meme[] recommend(int userId, int howManyMemes) {
        Meme m1 = new Meme();
        m1.addTag("cute");
        m1.setImgUrl("meow");
        m1.setUrl("http://www.google.de");
        m1.setTitle("it lives");
        Meme m2 = new Meme();
        m2.addTag("cute");
        m2.setImgUrl("meow");
        m2.setUrl("http://www.bing.de");
        m2.setTitle("arr, it be dead now...");

        Meme[] memes = new Meme[2];
        memes[0] = m1;
        memes[1] = m2;
        return memes;
    }

}
