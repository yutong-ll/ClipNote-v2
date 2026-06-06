package com.wo.clipnote.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// @Dao 告诉系统这是一个操作数据库的接口
@Dao
interface AppDao {

    // --- 下面是操作笔记表 (notes) 的方法 ---

    // @Query 里写的是 SQL 查询语句。意思是从 notes 表里拿所有数据，并按时间倒序排（最新的在最上面）
    // 返回值是 Flow。你可以把它当做前端 Vue 里的响应式变量，只要数据库一更新，它会自动把新数据推送到界面上！
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    // @Insert 代表插入数据。OnConflictStrategy.REPLACE 意思是如果碰巧遇到 ID 一样的，就覆盖替换掉它
    // suspend 关键字代表这是一个“耗时操作（协程）”，就像前端的 async/await，它会在后台默默执行，不会卡住页面。
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    // @Delete 代表删除传进来的这条数据
    @Delete
    suspend fun deleteNote(note: NoteEntity)


    // --- 下面是操作标签表 (tags) 的方法 ---

    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)
}