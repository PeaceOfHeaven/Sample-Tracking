package com.example.nhat.mapexample;

/**
 * Created by Nhat on 11/12/2016.
 */

public interface BaseView<T extends BasePresenter> {

    void setPresenter(T presenter);

}
