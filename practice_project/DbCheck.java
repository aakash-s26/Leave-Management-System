import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class DbCheck {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require";
        String user = "postgres.ifmpanygpspzxwpkfmsm";
        String pass = "Engageemergeexcel";

        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT username, password, role FROM users WHERE username = ?");
            stmt.setString(1, "admin");
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                System.out.println("NO_USER");
            } else {
                String hashed = rs.getString("password");
                System.out.println("USERNAME=" + rs.getString("username"));
                System.out.println("PASSWORD=" + hashed);
                System.out.println("ROLE=" + rs.getString("role"));
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                System.out.println("MATCH_ADMIN123=" + encoder.matches("admin123", hashed));
                System.out.println("MATCH_ADMIN124=" + encoder.matches("admin124", hashed));
            }
            rs.close();
            stmt.close();
        }
    }
}
