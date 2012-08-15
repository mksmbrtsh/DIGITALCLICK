package maximsblog.blogspot.com.digitalclick;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class ResultActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.resultactivity);
	    ImageView iv = (ImageView) findViewById(R.id.imageView1);
	    Bitmap result = BitmapFactory.decodeFile(new File( this.getExternalFilesDir(null)
					.getAbsolutePath(), "1.png").getAbsolutePath());
	    if(result!=null){
	    	iv.setImageBitmap(result);
	    }
	
	}

}
