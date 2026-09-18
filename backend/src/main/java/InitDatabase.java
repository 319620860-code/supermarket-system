import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * 独立运行初始化脚本（非 Spring 启动路径）。密码与库名可通过环境变量覆盖。
 */
public class InitDatabase {
    public static void main(String[] args) {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "123456");
        String sqlFile = System.getenv().getOrDefault("INIT_SQL_FILE", "d:/biyesheji/sql/init.sql");

        String url = String.format(
                "jdbc:mysql://%s:%s/?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true",
                host, port);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            Statement statement = connection.createStatement();

            StringBuilder sqlContent = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(sqlFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("--") && !line.trim().isEmpty()) {
                    sqlContent.append(line);
                    if (line.trim().endsWith(";")) {
                        String sql = sqlContent.toString();
                        statement.execute(sql);
                        sqlContent.setLength(0);
                    }
                }
            }
            reader.close();

            System.out.println("数据库初始化成功！");

            statement.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("数据库初始化失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
