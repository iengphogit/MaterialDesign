package tosmerl.avaboy.com.Retrofit;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by iengpho on 6/21/18.
 */

public interface TosmerEndpoint {

    @GET("index.php")
    Call<JsonObject> index();

    @GET("index.php")
    Call<JsonObject> index(@Query("search") String name);

}
