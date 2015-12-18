# AndroidCustomRefresh 
*** 
 
Android custom refresh layout for ``listview`` and ``recyclerview`` 

<img src="art/show.gif" /> 

# Usage 
*** 

In XML

```xml
    <com.wzq.customrefresh.CustomRefreshLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/custom_refresh">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/recycler"
            android:layout_width="match_parent"
            android:layout_height="match_parent">

        </android.support.v7.widget.RecyclerView>
    </com.wzq.customrefresh.CustomRefreshLayout> 
```

Attrs 

```xml
     <declare-styleable name="CustomRefreshLayout">
            <attr name="AnimBackColor" format="color" />
            <attr name="AnimSwipeColor" format="color" />
            <attr name="AnimCircleColor" format="color"/>
            <attr name="AnimRadius" format="integer"/>
            <attr name="AnimTextSize" format="integer"/>
            <attr name="AnimTextColor" format="color"/>
     </declare-styleable>
```

Add Listener 

```java
final CustomRefreshLayout crl = (CustomRefreshLayout) findViewById(R.id.custom_refresh);
        crl.setOnRefreshListener(new CustomRefreshLayout.OnCircleRefreshListener() {
            @Override
            public void completeRefresh() {

            }

            @Override
            public void refreshing() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        crl.finishRefreshing();
                    }
                }, 3000);
            }
        }); 
        
```
    
