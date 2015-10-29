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
            getMeme("http://9gag.com/gag/aZNoBBp?sc=cute", "http://img-9gag-fun.9cache.com/photo/aZNoBBp_700b.jpg", "Ew human.", "cute" ,"9gag"),
            getMeme("http://9gag.com/gag/aNK8ZqK?sc=cute", "http://img-9gag-fun.9cache.com/photo/aNK8ZqK_700b.jpg", "After one week away, this is her happy face of seeing me.", "cute" ,"9gag"),
            getMeme("http://9gag.com/gag/adprLxd?sc=cute", "http://img-9gag-fun.9cache.com/photo/adprLxd_700b.jpg", "Sad kitty is sad.", "cute" ,"9gag"),
            getMeme("http://9gag.com/gag/abbKM8E?sc=cute", "http://img-9gag-fun.9cache.com/photo/abbKM8E_700b.jpg", "Morag's unique ginger and black coat", "cute" ,"9gag"),
            getMeme("http://9gag.com/gag/aQnBqWq?sc=cute", "http://img-9gag-fun.9cache.com/photo/aQnBqWq_700b.jpg", "My Lion King", "cute" ,"9gag"),

            getMeme("http://9gag.com/gag/aDmrxgx?sc=cute", "http://img-9gag-fun.9cache.com/photo/aDmrxgx_700b.jpg", "Easy, breezy, beautiful, cover squirrel", "cute" ,"9gag"),
            getMeme("http://9gag.com/gag/aepy00b?sc=cute", "http://img-9gag-fun.9cache.com/photo/aepy00b_700b_v1.jpg", "This is my beauty from Namibia", "cute" ,"9gag"),
            getMeme("http://9gag.com/gag/a8jQM8e?sc=cute", "http://img-9gag-fun.9cache.com/photo/a8jQM8e_700b.jpg", "She is almost 10 years old but still has a baby face", "cute" ,"9gag"),
            getMeme("http://9gag.com/gag/aYwZK40?sc=cute", "http://img-9gag-fun.9cache.com/photo/aYwZK40_700b.jpg", "My best friend for life!", "cute" ,"9gag"),
            getMeme("http://9gag.com/gag/avL4n4d?sc=cute", "http://img-9gag-fun.9cache.com/photo/avL4n4d_700b.jpg", "Wanted to share my beautiful old lady with you.", "cute" ,"9gag"),
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
