package meme_recommender.request_handlers;

import de.ur.ahci.model.Meme;
import meme_recommender.RequestHandler;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Created by jonbr on 28.10.2015.
 */
public class AppRequestHandler extends RequestHandler {


    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out) {
        String uri = req.getRequestURI();
        if(!uri.endsWith(".json")) return false;

        uri = uri.substring(1);

        if (uri.startsWith("load_images")) {
            out.write(getRecommendedImages());
        } else if (uri.startsWith("sign_in")) {
            out.write("{status: \"accept\"}");
        } else if (uri.startsWith("test_storage"))  {

        } else {
            out.write("{}");
        }

        out.flush();
        out.close();

        return true;
    }

    /*
        http://9gag.com/gag/aZNoBBp?sc=cute	http://img-9gag-fun.9cache.com/photo/aZNoBBp_700b.jpg	[cute,9gag]	Ew human.
        http://9gag.com/gag/aNK8ZqK?sc=cute	http://img-9gag-fun.9cache.com/photo/aNK8ZqK_700b.jpg	[cute,9gag]	After one week away, this is her happy face of seeing me.
        http://9gag.com/gag/adprLxd?sc=cute	http://img-9gag-fun.9cache.com/photo/adprLxd_700b.jpg	[cute,9gag]	Sad kitty is sad.
        http://9gag.com/gag/abbKM8E?sc=cute	http://img-9gag-fun.9cache.com/photo/abbKM8E_700b.jpg	[cute,9gag]	Morag's unique ginger and black coat
        http://9gag.com/gag/aQnBqWq?sc=cute	http://img-9gag-fun.9cache.com/photo/aQnBqWq_700b.jpg	[cute,9gag]	My Lion King

        http://9gag.com/gag/aDmrxgx?sc=cute	http://img-9gag-fun.9cache.com/photo/aDmrxgx_700b.jpg	[cute,9gag]	Easy, breezy, beautiful, cover squirrel
        http://9gag.com/gag/aepy00b?sc=cute	http://img-9gag-fun.9cache.com/photo/aepy00b_700b_v1.jpg	[cute,9gag]	This is my beauty from Namibia
        http://9gag.com/gag/a8jQM8e?sc=cute	http://img-9gag-fun.9cache.com/photo/a8jQM8e_700b.jpg	[cute,9gag]	She is almost 10 years old but still has a baby face
        http://9gag.com/gag/aYwZK40?sc=cute	http://img-9gag-fun.9cache.com/photo/aYwZK40_700b.jpg	[cute,9gag]	My best friend for life!
        http://9gag.com/gag/avL4n4d?sc=cute	http://img-9gag-fun.9cache.com/photo/avL4n4d_700b.jpg	[cute,9gag]	Wanted to share my beautiful old lady with you.

     */

    private String getRecommendedImages() {

        Meme[] memes = new Meme[]{
                getMeme("http://9gag.com/gag/aZNoBBp?sc=cute","http://img-9gag-fun.9cache.com/photo/aZNoBBp_700b.jpg","Ew human","9gag","cute"),
                getMeme("http://9gag.com/gag/a1YnGBD?sc=comic","http://img-9gag-fun.9cache.com/photo/a1YnGBD_700b.jpg","Good Hair Day","9gag","comic"),
                getMeme("http://9gag.com/gag/aNKX956","http://img-9gag-fun.9cache.com/photo/aNKX956_460s.jpg","Dog taking his teddy to bed","9gag","cute"),
                getMeme("http://9gag.com/gag/286508","http://img-9gag-fun.9cache.com/photo/286508_700b.jpg","Socially Awkward Penguin","9gag","meme"),
                getMeme("http://9gag.com/gag/a7KexQq","http://img-9gag-fun.9cache.com/photo/a7KexQq_700b_v1.jpg","What do you see in the Mirror, Professor Dumbledore?","9gag","comic"),
                getMeme("http://9gag.com/gag/aB3exy2","http://img-9gag-fun.9cache.com/photo/aB3exy2_700b.jpg","Shiba in a sombrero","9gag","cute"),
                getMeme("http://9gag.com/gag/apBnWP8?sc=comic","http://img-9gag-fun.9cache.com/photo/apBnWP8_700b.jpg","Something good","9gag","comic"),
                getMeme("http://9gag.com/gag/aynZBd8?sc=comic","http://img-9gag-fun.9cache.com/photo/aynZBd8_700b.jpg","When the research went too far","9gag","comic"),
                getMeme("http://9gag.com/gag/a2qAQMY","http://img-9gag-fun.9cache.com/photo/a2qAQMY_700b.jpg","Make it behave, hooman","9gag","cute"),
                getMeme("http://9gag.com/gag/am8yKzv","http://img-9gag-fun.9cache.com/photo/am8yKzv_700b.jpg","15-day-old hamster babies","9gag","cute"),
                getMeme("http://9gag.com/gag/aRV9vZM?sc=comic","http://img-9gag-fun.9cache.com/photo/aRV9vZM_700b_v1.jpg","Game of Thrones in nutshell","9gag","comic"),
                getMeme("http://9gag.com/gag/a9P4OAZ?sc=comic","http://img-9gag-fun.9cache.com/photo/a9P4OAZ_700b_v3.jpg","It is complete","9gag","comic"),
                getMeme("http://9gag.com/gag/aKByLW6","http://img-9gag-fun.9cache.com/photo/aKByLW6_700b.jpg","Cats and boxes - Not every box is empty.","9gag","cute"),
                getMeme("http://9gag.com/gag/5090489","http://img-9gag-fun.9cache.com/photo/5090489_700b.jpg","Scumbag Steve is Scumbag!","9gag","meme"),
                getMeme("http://9gag.com/gag/a6Lzxnm","http://img-9gag-fun.9cache.com/photo/a6Lzxnm_460s.jpg","Happy dance","9gag","cute"),

                getMeme("http://9gag.com/gag/a2q9ob9?sc=comic","http://img-9gag-fun.9cache.com/photo/a2q9ob9_700b_v1.jpg","The future of FPS","9gag","comic"),
                getMeme("http://9gag.com/gag/aEzED5M?sc=comic","http://img-9gag-fun.9cache.com/photo/aEzED5M_700b.jpg","Click wisely","9gag","comic"),
                getMeme("http://9gag.com/gag/a2qyZ0O","http://img-9gag-fun.9cache.com/photo/a2qyZ0O_700b_v2.jpg","Dumbledore is savage af","9gag","comic"),
                getMeme("http://9gag.com/gag/anXEbe0","http://img-9gag-fun.9cache.com/photo/anXEbe0_700b_v1.jpg","If Programming Languages Were Weapons","9gag","meme"),
                getMeme("http://9gag.com/gag/aepE8PW?sc=1","http://img-9gag-fun.9cache.com/photo/aepE8PW_460s.jpg","Looks like the cat's in the bag.","9gag","cute"),
                getMeme("http://9gag.com/gag/azVQORm?sc=comic","http://img-9gag-fun.9cache.com/photo/azVQORm_700b_v1.jpg","Just read this...","9gag","comic"),
                getMeme("http://9gag.com/gag/aGRbqYz?sc=comic","http://img-9gag-fun.9cache.com/photo/aGRbqYz_700b_v1.jpg","What playing video games taught me","9gag","comic"),
                getMeme("http://9gag.com/gag/a3LedEr?sc=comic","http://img-9gag-fun.9cache.com/photo/a3LedEr_700b.jpg","Daily ritual","9gag","comic"),
                getMeme("http://9gag.com/gag/a7bm6Xr?sc=comic","http://img-9gag-fun.9cache.com/photo/a7bm6Xr_700b_v1.jpg","Right in the feels! *cries in Spanish*","9gag","comic"),
                getMeme("http://9gag.com/gag/aKByDmb?sc=comic","http://img-9gag-fun.9cache.com/photo/aKByDmb_700b_v1.jpg","This is why I loved juice with ice as a kid.","9gag","comic"),
                getMeme("http://9gag.com/gag/anB3Yg0?sc=comic","http://img-9gag-fun.9cache.com/photo/anB3Yg0_700b_v1.jpg","Just having a dinner...","9gag","comic"),
                getMeme("http://9gag.com/gag/agNbZL6?sc=comic","http://img-9gag-fun.9cache.com/photo/agNbZL6_700b.jpg","Every Time I Download Mods","9gag","comic"),
                getMeme("http://9gag.com/gag/a1A3jAP","http://img-9gag-fun.9cache.com/photo/a1A3jAP_460s_v1.jpg","Wiggle Wiggle Wiggle","9gag","cute"),
                getMeme("http://9gag.com/gag/aDmNLXK?sc=comic","http://img-9gag-fun.9cache.com/photo/aDmNLXK_700b.jpg","People Who Like Saddest Turtle","9gag","comic"),
                getMeme("http://9gag.com/gag/aEzjNpK?sc=comic","http://img-9gag-fun.9cache.com/photo/aEzjNpK_700b_v1.jpg","Every... Damn... Time","9gag","comic"),

                getMeme("http://9gag.com/gag/agNpdor?sc=comic","http://img-9gag-fun.9cache.com/photo/agNpdor_700b.jpg","Persuasion and Doors (By Classic Randy)","9gag","comic"),
                getMeme("http://9gag.com/gag/a6L4A6m?sc=comic","http://img-9gag-fun.9cache.com/photo/a6L4A6m_700b.jpg","Elevator","9gag","comic"),
                getMeme("http://9gag.com/gag/ajnGOGw?sc=comic","http://img-9gag-fun.9cache.com/photo/ajnGOGw_700b.jpg","Immortality","9gag","comic"),
                getMeme("http://9gag.com/gag/aPG0nrw","http://img-9gag-fun.9cache.com/photo/aPG0nrw_460s.jpg","Whack-A-Cat","9gag","cute"),
                getMeme("http://9gag.com/gag/aWON0v4?sc=comic","http://img-9gag-fun.9cache.com/photo/aWON0v4_700b_v1.jpg","Instructions unclear.","9gag","comic"),
                getMeme("http://9gag.com/gag/abbm6Xb?sc=comic","http://img-9gag-fun.9cache.com/photo/abbm6Xb_700b_v1.jpg","Cat's logic","9gag","comic"),
                getMeme("http://9gag.com/gag/aXXzM82","http://img-9gag-fun.9cache.com/photo/aXXzM82_460s.jpg","Hey Bear... Hey Human!","9gag","cute"),
                getMeme("http://9gag.com/gag/aLB3zzW","http://img-9gag-fun.9cache.com/photo/aLB3zzW_460s.jpg","*Flumph!*","9gag","cute"),
                getMeme("http://9gag.com/gag/a2qVbvw","http://img-9gag-fun.9cache.com/photo/a2qVbvw_700b.jpg","They have no regard for stop signs either","9gag","meme"),
                getMeme("http://9gag.com/gag/aq2O6dR","http://img-9gag-fun.9cache.com/photo/aq2O6dR_700b_v1.jpg","When you introduce a dog to an 8 year old cat","9gag","cute"),
                getMeme("http://9gag.com/gag/azVBDxN?sc=comic","http://img-9gag-fun.9cache.com/photo/azVBDxN_700b.jpg","I Think I'm Happy","9gag","comic"),
                getMeme("http://9gag.com/gag/arR0mNV","http://img-9gag-fun.9cache.com/photo/arR0mNV_700b_v1.jpg","Day 133, They still suspect nothing","9gag","cute"),
                getMeme("http://9gag.com/gag/a9Pe6AK?sc=meme","http://img-9gag-fun.9cache.com/photo/a9Pe6AK_700b_v2.jpg","There is no problem which can't be solved","9gag","meme"),
                getMeme("http://9gag.com/gag/aNKXR3G","http://img-9gag-fun.9cache.com/photo/aNKXR3G_700b.jpg","Just me? okay...","9gag","meme"),
                getMeme("http://9gag.com/gag/a8j3NPp","http://img-9gag-fun.9cache.com/photo/a8j3NPp_700b.jpg","10 Points to Dumbledore","9gag","comic"),

                getMeme("http://9gag.com/gag/aAp7zZg","http://img-9gag-fun.9cache.com/photo/aAp7zZg_460s.jpg","Quick brown fox jumps over the lazy dog","9gag","cute"),
                getMeme("http://9gag.com/gag/aDmLeMZ?sc=1","http://img-9gag-fun.9cache.com/photo/aDmLeMZ_460s.jpg","A Dog Doing Tricks On A Trampoline","9gag","cute"),
                getMeme("http://9gag.com/gag/a3LDA33","http://img-9gag-fun.9cache.com/photo/a3LDA33_460s.jpg","All smartphones should power on like this","9gag","cute"),
                getMeme("http://9gag.com/gag/aXbKVrP","http://img-9gag-fun.9cache.com/photo/aXbKVrP_460s.jpg","It's a lovely garden you have there. Would be a shame if something were to... happen to it...","9gag","cute"),
                getMeme("http://9gag.com/gag/aAp769d","http://img-9gag-fun.9cache.com/photo/aAp769d_460s.jpg","Parents will relate","9gag","cute"),
                getMeme("http://9gag.com/gag/aMQ6AW1","http://img-9gag-fun.9cache.com/photo/aMQ6AW1_460s.jpg","Petting An Owl.","9gag","cute"),
                getMeme("http://9gag.com/gag/aRV8bYB","http://img-9gag-fun.9cache.com/photo/aRV8bYB_460s.jpg","I'm often too lazy to eat","9gag","cute"),
                getMeme("http://9gag.com/gag/aYp2mo2","http://img-9gag-fun.9cache.com/photo/aYp2mo2_460s.jpg","If you've ever wondered what Programming is like, This about sums it up.","9gag","meme"),
                getMeme("http://9gag.com/gag/abyvZLb","http://img-9gag-fun.9cache.com/photo/abyvZLb_700b.jpg","Schrodinger's Troll","9gag","meme")
        };


        StringBuilder builder = new StringBuilder();
        builder.append("{\n");

        builder.append("\timages: [\n");

        for(int i = 0; i < memes.length; i++) {

            builder.append("\t\t{\n");

            builder.append("\t\t\tid: " + i + ",\n");
            builder.append("\t\t\turl: \"" + memes[i].getUrl() + "\",\n");
            builder.append("\t\t\ttitle: \"" + memes[i].getTitle() + "\"\n");

            builder.append("\t\t}");
            if(i != memes.length - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }

        builder.append("\t]\n");
        builder.append("}\n");
        return builder.toString();
    }

    private Meme getMeme(String url, String imgUrl, String title, String... tags) {
        Meme m = new Meme();
        m.setImgUrl(imgUrl);
        m.setTitle(title);
        m.setUrl(url);
        for(String t : tags) {
            m.addTag(t);
        }
        return m;
    }

}
