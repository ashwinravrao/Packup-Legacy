package com.ashwinrao.locrate.data.repo;

import com.ashwinrao.locrate.data.AppDatabase;
import com.ashwinrao.locrate.data.repo.dao.MoveDao;
import com.ashwinrao.locrate.data.model.Move;
import com.ashwinrao.locrate.data.AsyncTasks;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MoveRepository {

    private MoveDao dao;

    @Inject
    public MoveRepository(AppDatabase database) {
        dao = database.moveDao();
    }

    public void insert(Move... moves) {
        new AsyncTasks.MoveAsyncTask(dao, "insert").execute(moves);
    }

    public void update(Move... moves) {
        new AsyncTasks.MoveAsyncTask(dao, "update").execute(moves);
    }

    public void delete(Move... moves) {
        new AsyncTasks.MoveAsyncTask(dao, "delete").execute(moves);
    }

}
