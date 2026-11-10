package co.edu.uts.servicios.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Persistencia local pequeña; el perfil y el historial se guardan juntos. */
class LocalStudentRepository(context: Context) : StudentRepository {
    private val prefs = context.getSharedPreferences("uts_portal_v1", Context.MODE_PRIVATE)

    override fun load(): PortalData {
        val raw = prefs.getString("data", null) ?: return PortalData()
        val json = JSONObject(raw)
        val student = json.optJSONObject("student")?.toStudent()
        val rows = json.getJSONArray("enrollments")
        return PortalData(student, List(rows.length()) { i ->
            val row = rows.getJSONObject(i)
            Enrollment(row.getString("id"), row.getString("course"), row.getJSONObject("student").toStudent(),
                row.getBoolean("scholarship"), row.getBoolean("icetex"),
                Quote(row.getLong("base"), row.getLong("discount"), row.getLong("surcharge"), row.getLong("total")),
                row.getLong("createdAt"))
        })
    }

    override fun save(data: PortalData) {
        val rows = JSONArray()
        data.enrollments.forEach { e ->
            rows.put(JSONObject().put("id", e.id).put("course", e.courseTitle)
                .put("student", e.student.toJson()).put("scholarship", e.scholarship).put("icetex", e.icetex)
                .put("base", e.quote.base).put("discount", e.quote.discount).put("surcharge", e.quote.surcharge)
                .put("total", e.quote.total).put("createdAt", e.createdAt))
        }
        prefs.edit().putString("data", JSONObject().put("student", data.student?.toJson())
            .put("enrollments", rows).toString()).apply()
    }

    private fun Student.toJson() = JSONObject().put("name", name).put("code", code)
        .put("faculty", faculty.name).put("email", email)
    private fun JSONObject.toStudent() = Student(getString("name"), getString("code"),
        Faculty.valueOf(getString("faculty")), getString("email"))
}
