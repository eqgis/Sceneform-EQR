package com.eqgis.test.samples;

import static android.text.TextUtils.isEmpty;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eqgis.eqr.Location;
import com.eqgis.eqr.core.CoordinateUtils;
import com.eqgis.test.R;
import com.google.sceneform.math.Vector3;

/**
 * 地理坐标与场景相对坐标转换示例
 *
 * <p>该示例展示了如何基于参考地理点（经纬度、高程）与方位角，
 * 使用 {@link com.eqgis.eqr.core.CoordinateUtils} 在地理坐标系
 * （CGCS2000 经纬度）与三维场景相对坐标系（X/Y/Z）之间进行双向转换。</p>
 *
 * <p>主要特性包括：</p>
 * <ul>
 *     <li>支持地理坐标（经度、纬度、高度）到场景相对坐标的转换</li>
 *     <li>支持场景相对坐标（X、Y、Z）反算为地理坐标</li>
 *     <li>以参考点 {@link com.eqgis.eqr.Location} 作为坐标原点进行计算</li>
 * </ul>
 *
 * <p>本类主要用于验证地理坐标与三维引擎坐标之间的换算逻辑，
 * 适用于 GIS + 3D 场景、数字孪生、AR/VR 场景中
 * “真实世界坐标 → 虚拟空间坐标” 的基础定位与对齐计算。</p>
 */

public class CoordinateConvertActivity extends AppCompatActivity {

    private RadioGroup rgMode;
    private LinearLayout layoutGeo, layoutRel;
    private Button btnConvert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinate_convert);

        rgMode = findViewById(R.id.rg_mode);
        layoutGeo = findViewById(R.id.layout_geo);
        layoutRel = findViewById(R.id.layout_rel);
        btnConvert = findViewById(R.id.btn_convert);

        // 默认：地理 → 相对
        updateMode(true);

        rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_geo_to_rel) {
                updateMode(true);
            } else {
                updateMode(false);
            }
        });

        btnConvert.setOnClickListener(v -> {
            try {
                if (rgMode.getCheckedRadioButtonId() == R.id.rb_geo_to_rel) {
                    convertGeoToRel();
                } else {
                    convertRelToGeo();
                }
            }catch (NullPointerException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMode(boolean geoToRel) {
        setLayoutEnabled(layoutGeo, geoToRel);
        setLayoutEnabled(layoutRel, !geoToRel);
    }

    private void setLayoutEnabled(ViewGroup layout, boolean enabled) {
        layout.setAlpha(enabled ? 1f : 0.4f);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);
            v.setEnabled(enabled);
            if (v instanceof ViewGroup) {
                setLayoutEnabled((ViewGroup) v, enabled);
            }
        }
    }

    private void convertGeoToRel() {

        EditText lon1Text = findViewById(R.id.et_lon1);
        EditText lat1Text = findViewById(R.id.et_lat1);
        EditText h1Text   = findViewById(R.id.et_h1);
        EditText azText   = findViewById(R.id.et_azimuth);

        EditText lon2Text = findViewById(R.id.et_lon2);
        EditText lat2Text = findViewById(R.id.et_lat2);
        EditText h2Text   = findViewById(R.id.et_h2);

        // ===== 解析 =====
        double lon1 = parseDouble(lon1Text, "参考点经度");
        double lat1 = parseDouble(lat1Text, "参考点纬度");
        double lon2 = parseDouble(lon2Text, "目标点经度");
        double lat2 = parseDouble(lat2Text, "目标点纬度");
        double azimuth = parseAzimuth(azText);

        // ===== 合规校验 =====
        if (!checkLongitude(lon1Text, lon1)) return;
        if (!checkLatitude(lat1Text, lat1)) return;
        if (!checkLongitude(lon2Text, lon2)) return;
        if (!checkLatitude(lat2Text, lat2)) return;

        // ===== 高度（允许为空，默认 0）=====
        double h1 = isEmpty(h1Text.getText()) ? 0 : parseDouble(h1Text, "参考点高度");
        double h2 = isEmpty(h2Text.getText()) ? 0 : parseDouble(h2Text, "目标点高度");

        // ===== 构造对象 =====
        Location ref = new Location(lon1, lat1, h1);
        Location target = new Location(lon2, lat2, h2);

        // ===== 坐标转换 =====
        Vector3 result = CoordinateUtils.toScenePosition(
                ref,
                target,
                azimuth
        );

        // ===== 输出 =====
        ((EditText) findViewById(R.id.et_x)).setText(String.valueOf(result.x));
        ((EditText) findViewById(R.id.et_y)).setText(String.valueOf(result.y));
        ((EditText) findViewById(R.id.et_z)).setText(String.valueOf(result.z));
    }


    private void convertRelToGeo() {
        EditText lon1Text = findViewById(R.id.et_lon1);
        EditText lat1Text = findViewById(R.id.et_lat1);
        EditText h1Text   = findViewById(R.id.et_h1);
        EditText azText   = findViewById(R.id.et_azimuth);

        EditText xText = findViewById(R.id.et_x);
        EditText yText = findViewById(R.id.et_y);
        EditText zText = findViewById(R.id.et_z);

        // ===== 解析 =====
        double lon1 = parseDouble(lon1Text, "参考点经度");
        double lat1 = parseDouble(lat1Text, "参考点纬度");
        double azimuth = parseAzimuth(azText);
        double x = parseDouble(xText, "X坐标");
        double y = parseDouble(yText, "Y坐标");
        double z = parseDouble(zText, "Z坐标");

        // ===== 高度（允许为空，默认0） =====
        double h1 = isEmpty(h1Text.getText()) ? 0 : parseDouble(h1Text, "参考点高度");

        // ===== 经纬度合规校验 =====
        if (!checkLongitude(lon1Text, lon1)) return;
        if (!checkLatitude(lat1Text, lat1)) return;
        if (azimuth < 0 || azimuth > 360) {
            Toast.makeText(this, "方位角应在0~360°之间", Toast.LENGTH_SHORT).show();
            azText.setText(null);
            return;
        }

        // ===== 构造参考点 =====
        Location ref = new Location(lon1, lat1, h1);

        // ===== 坐标转换 =====
        Vector3 vector3 = new Vector3((float) x, (float) y, (float) z);
        Location result = CoordinateUtils.toGeoLocation(ref, vector3, azimuth);

        // ===== 输出 =====
        ((EditText) findViewById(R.id.et_lon2)).setText(String.valueOf(result.getX()));
        ((EditText) findViewById(R.id.et_lat2)).setText(String.valueOf(result.getY()));
        ((EditText) findViewById(R.id.et_h2)).setText(String.valueOf(result.getZ()));
    }

    //<editor-fold>
    private double parseDouble(EditText et, String name) {
        String s = et.getText().toString().trim();
        if (s.isEmpty()) {
            et.setText(null);
            et.requestFocus();
            throw new NullPointerException(name + " 不能为空");
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            Toast.makeText(this, name + " 输入格式错误", Toast.LENGTH_SHORT).show();
            et.setText(null);
            et.requestFocus();
            return 0;
        }
    }


    private boolean checkLongitude(EditText et, double lon) {
        if (lon < -180 || lon > 180) {
            Toast.makeText(this, "经度必须在 [-180, 180]", Toast.LENGTH_SHORT).show();
            et.setText(null);
            et.requestFocus();
            return false;
        }
        return true;
    }


    private boolean checkLatitude(EditText et, double lat) {
        if (lat < -90 || lat > 90) {
            Toast.makeText(this, "纬度必须在 [-90, 90]", Toast.LENGTH_SHORT).show();
            et.setText(null);
            et.requestFocus();
            return false;
        }
        return true;
    }


    private Double parseAzimuth(EditText et) {
        Double az = parseDouble(et, "方位角");
        if (az == null) return null;

        az = az % 360.0;
        if (az < 0) az += 360.0;
        return az;
    }
    //</editor-fold>
}

