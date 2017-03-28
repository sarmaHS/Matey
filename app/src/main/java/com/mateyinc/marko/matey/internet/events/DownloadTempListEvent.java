package com.mateyinc.marko.matey.internet.events;

import com.mateyinc.marko.matey.data.TemporaryDataAccess;
import com.mateyinc.marko.matey.internet.operations.OperationType;
import com.mateyinc.marko.matey.model.MModel;


public class DownloadTempListEvent<T extends MModel> extends Event{

    public final TemporaryDataAccess<T> mDao;
    public final MModel mModel;

    public DownloadTempListEvent(boolean isSuccess, OperationType eventType, TemporaryDataAccess<T> dao, MModel model){
        super(isSuccess, eventType);
        this.mDao = dao;
        this.mModel = model;
    }
}