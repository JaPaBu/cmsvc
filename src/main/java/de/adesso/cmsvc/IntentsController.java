package de.adesso.cmsvc;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/api/intents")
public class IntentsController {

    private final
    IntentControllerUtil intentControllerUtil;

    @Autowired
    public IntentsController(IntentControllerUtil intentControllerUtil) {
        this.intentControllerUtil = intentControllerUtil;
    }

    @GetMapping
    public void getIntents(HttpServletResponse response) throws IOException {
        intentControllerUtil.writeExcel(response.getOutputStream());
    }

    @PostMapping
    public void postIntents(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException, InvalidFormatException, FileUploadException {
        response.sendRedirect("/");
        intentControllerUtil.readExcel(file.getInputStream());
    }
}
