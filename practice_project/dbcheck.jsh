import java.sql.*;

String url = "jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require";
String user = "postgres.ifmpanygpspzxwpkfmsm";
String pass = "Engageemergeexcel";

Connection conn = DriverManager.getConnection(url, user, pass);
PreparedStatement stmt = conn.prepareStatement("SELECT username, password, role FROM users WHERE username = ?");
stmt.setString(1, "admin");
ResultSet rs = stmt.executeQuery();
if (!rs.next()) {
    System.out.println("NO_USER");
} else {
    System.out.println("USERNAME=" + rs.getString("username"));
    System.out.println("PASSWORD=" + rs.getString("password"));
    System.out.println("ROLE=" + rs.getString("role"));
}
rs.close();
stmt.close();
conn.close();
