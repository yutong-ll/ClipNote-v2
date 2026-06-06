package com.wo.clipnote.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity 告诉 Room，这是一个名叫 "tags" 的数据表（Excel 工作表）
@Entity(tableName = "tags")
data class TagEntity(
    // @PrimaryKey(autoGenerate = true) 意思是这是表的主键（唯一标识），
    // 就像数据的身份证号。autoGenerate = true 让系统自动从 1, 2, 3 往下排，不用我们操心。
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 下面就是普通的字段（表头）
    val name: String,  // 标签名字，比如 "前端"
    val color: String  // 标签颜色，存十六进制色值，比如 "#FF0000"
)