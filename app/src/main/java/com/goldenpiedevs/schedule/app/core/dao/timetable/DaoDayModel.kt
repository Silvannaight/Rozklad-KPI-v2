package com.goldenpiedevs.schedule.app.core.dao.timetable

import com.goldenpiedevs.schedule.app.core.notifications.manger.NotificationManager
import com.google.gson.annotations.SerializedName
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class DaoDayModel : RealmObject() {

    @PrimaryKey
    var uuid = UUID.randomUUID().toString()

    var dayNumber = "-1"
    var dayName = ""
    var weekNumber = "-1"
    @SerializedName("lessons")
    var lessons: RealmList<DaoLessonModel> = RealmList()
    var parentGroup = ""
    var parentTeacherId = "-1"

    companion object {
//        fun firstWeek(): List<DaoDayModel> {
//            val realm = Realm.getDefaultInstance()
//            val lessonModel = realm.copyFromRealm(realm.where(DaoDayModel::class.java)
//
//                    .equalTo("weekNumber", 1.toString()).findAll())
//
//            if (!realm.isClosed)
//                realm.close()
//
//            return lessonModel
//        }
//
//        fun secondWeek(): List<DaoDayModel> {
//            val realm = Realm.getDefaultInstance()
//            val lessonModel = realm.copyFromRealm(realm.where(DaoDayModel::class.java)
//                    .equalTo("weekNumber", 2.toString()).findAll())
//
//            if (!realm.isClosed)
//                realm.close()
//
//            return lessonModel
//        }

//        fun firstWeekForTeacher(teacherID: String): Collection<DaoDayModel> {
//            val realm = Realm.getDefaultInstance()
//            val lessonModel = realm.copyFromRealm(realm.where(DaoDayModel::class.java)
//                    .equalTo("weekNumber", 1.toString())
//                    .equalTo("parentTeacherId", teacherID).findAll())
//
//            if (!realm.isClosed)
//                realm.close()
//
//            return lessonModel
//        }
//
//        fun secondWeekForTeacher(teacherID: String): Collection<DaoDayModel> {
//            val realm = Realm.getDefaultInstance()
//            val lessonModel = realm.copyFromRealm(realm.where(DaoDayModel::class.java)
//                    .equalTo("weekNumber", 2.toString())
//                    .equalTo("parentTeacherId", teacherID).findAll())
//
//            if (!realm.isClosed)
//                realm.close()
//
//            return lessonModel
//        }

        fun saveGroupTimeTable(key: ArrayList<DaoLessonModel>, groupName: String, notificationManager: NotificationManager) {
            val realm = Realm.getDefaultInstance()

            key.groupBy { it.lessonWeek.toInt() }.forEach { (weekNum, weekLessonsList) ->
                weekLessonsList.groupBy { it.dayNumber.toInt() }.forEach { (dayNum, dayLessonsList) ->
                    val model = realm.where(DaoDayModel::class.java).equalTo("parentGroup", groupName)
                            .equalTo("dayNumber", dayNum.toString())
                            .equalTo("weekNumber", weekNum.toString()).findFirst()
                    model?.let { modelIt ->
                        realm.executeTransaction {
                            modelIt.lessons.deleteAllFromRealm()
                            modelIt.lessons.addAll(dayLessonsList)
                            it.copyToRealmOrUpdate(modelIt)
                        }

                        notificationManager.createNotification(dayLessonsList)
                    } ?: run {
                        realm.executeTransaction {
                            it.copyToRealmOrUpdate(DaoDayModel().apply {
                                lessons.addAll(dayLessonsList)
                                parentGroup = groupName
                                weekNumber = weekNum.toString()
                                dayNumber = dayNum.toString()
                                dayName = dayLessonsList.first().dayName
                            })

                            notificationManager.createNotification(dayLessonsList)
                        }
                    }
                }
            }
        }

        fun saveTeacherTimeTable(key: ArrayList<DaoLessonModel>, teacherId: Int) {
            val realm = Realm.getDefaultInstance()

            key.groupBy { it.lessonWeek.toInt() }.forEach { (weekNum, weekLessonsList) ->
                weekLessonsList.groupBy { it.dayNumber.toInt() }.forEach { (dayNum, dayLessonsList) ->
                    val model = realm.where(DaoDayModel::class.java).equalTo("parentTeacherId", teacherId.toString())
                            .equalTo("dayNumber", dayNum.toString())
                            .equalTo("weekNumber", weekNum.toString()).findFirst()
                    model?.let { modelIt ->
                        realm.executeTransaction {
                            modelIt.lessons.deleteAllFromRealm()
                            modelIt.lessons.addAll(dayLessonsList)
                            it.copyToRealmOrUpdate(modelIt)
                        }
                    } ?: run {
                        realm.executeTransaction {
                            it.copyToRealmOrUpdate(DaoDayModel().apply {
                                lessons.addAll(dayLessonsList)
                                parentTeacherId = teacherId.toString()
                                weekNumber = weekNum.toString()
                                dayNumber = dayNum.toString()
                                dayName = dayLessonsList.first().dayName
                            })
                        }
                    }
                }
            }
        }
    }
}