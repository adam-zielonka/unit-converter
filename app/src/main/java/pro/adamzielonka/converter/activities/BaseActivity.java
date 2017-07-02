package pro.adamzielonka.converter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.util.List;

import pro.adamzielonka.converter.R;
import pro.adamzielonka.converter.units.Measures;
import pro.adamzielonka.converter.units.Units;

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    NavigationView mNavigationView;
    int mItemId;
    List<Units> unitsList;

    @Override
    public void setContentView(int layoutResID) {
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = (FrameLayout) mDrawerLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(mDrawerLayout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        mNavigationView.setNavigationItemSelectedListener(this);

        createConvertersMenu();
    }

    private void createConvertersMenu() {
        Menu menu = mNavigationView.getMenu();
        Menu convertersMenu = menu.addSubMenu(getString(R.string.nav_converters));

        Measures measures = Measures.getInstance();
        this.unitsList = measures.getUnitsList();

        int i = 0;
        for (Units units : this.unitsList) {
            MenuItem menuItem = convertersMenu.add(0, i + 1000, 0, units.getName());
            menuItem.setCheckable(true);
            i++;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        mDrawerLayout.closeDrawer(GravityCompat.START);

        if (id != mItemId) {
            switch (id) {
                case R.id.nav_settings:
                    Intent settings = new Intent(this.getBaseContext(), SettingsActivity.class);
                    startActivity(settings);
                    break;
                default:
                    Intent converter = new Intent(this.getBaseContext(), ConverterActivity.class);
                    converter.putExtra("converterNavId", id);
                    startActivity(converter);
                    finish();
            }
        }

        return true;
    }
}
