package com.wo.clipnote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// @Database 声明这是数据库大管家。把我们刚才建的 NoteEntity 和 TagEntity 放进来。version=1 是版本号。
@Database(entities = [NoteEntity::class, TagEntity::class], version = 1)
// @TypeConverters 告诉数据库，遇到不认识的数组时，去找 Converters 这个文件求救
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // 把我们刚才写的 DAO 接口暴露出来，让后面写业务逻辑的人可以调用
    abstract fun appDao(): AppDao

    // companion object 类似前端的静态方法 (static)
    companion object {
        // @Volatile 保证多线程环境下，这个变量始终是最新的
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 获取数据库实例的规范写法（单例模式）
        fun getDatabase(context: Context): AppDatabase {
            // 如果 INSTANCE 不是 null，就直接返回它；如果是 null，就走 synchronized 里的逻辑去创建一个
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clipnote_database" // 这是最后生成在手机存储里的真实数据库文件名
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}