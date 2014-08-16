package cc.notsoclever.tools;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Created by tjs on 8/3/14.
 */
public class SimpleServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
          throws ServletException, IOException {

        response.setContentType("text/html");
        setCookies(request, response);
        PrintWriter out = response.getWriter();
        out.println(load());
    }

    private void setCookies(HttpServletRequest request, HttpServletResponse response) {
        String[] params = request.getPathInfo().split("/");
        // the first element is empty since the string starts with an /
        System.out.println("params = " + Arrays.toString(params));
        if (params.length == 6) {
            response.addCookie(new Cookie("activeTab", params[3]));
            response.addCookie(new Cookie("oldTag", params[4]));
            response.addCookie(new Cookie("newTag", params[5]));
        }

        if (params.length >= 4) {
            response.addCookie(new Cookie("org", params[1]));
            response.addCookie(new Cookie("proj", params[2]));
        }
    }

    private String load() throws IOException {
        String pathStr = System.getProperty("user.dir") + "/target/version-diff-1.0-SNAPSHOT/index.html";
        Path path = FileSystems.getDefault().getPath(pathStr);

        return new String(Files.readAllBytes(path));
    }

}
