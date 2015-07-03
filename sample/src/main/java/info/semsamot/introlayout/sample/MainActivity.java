package info.semsamot.introlayout.sample;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.semsamot.introlayout.IntroController;
import info.semsamot.introlayout.IntroLayout;


public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.btn1) Button btn1;
    @InjectView(R.id.btn2) Button btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        IntroController introController = new IntroController(this);
        introController.getIntroLayout().setTargetShapeType(IntroLayout.ShapeType.SHAPE_RECTANGLE);
        introController.setTargets(new View[]{btn1, btn2});
        introController.setMessages(new String[]{"This is a test.", "This is a sample."});
        introController.startShow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
