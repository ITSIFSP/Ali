package com.here.infotransctd.Interdictions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.here.infotransctd.R;
import java.util.ArrayList;
import java.util.List;

public class InterdictionsList extends AppCompatActivity
{
    private List<Interdiction> interdictions = new ArrayList<>();
    private ListView lvInterdictions;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interdictions_list);
        lvInterdictions = findViewById(R.id.lvInterdictions);
        try
        {
            Intent i = getIntent();
            interdictions = (List<Interdiction>) i.getSerializableExtra("interdictions");
            lvInterdictions.setAdapter(new InterdictionsAdapter(this, interdictions));
            TextView select;
            if(interdictions.size() == 0) select = findViewById(R.id.txtNoInterdictions);
            else select = findViewById(R.id.txtInterdictions);
            select.setVisibility(View.VISIBLE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
