package nl.thehyve.ocdu.controllers;

import nl.thehyve.ocdu.OCEnvironmentsConfig;
import nl.thehyve.ocdu.models.OcItemMapping;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.services.FileService;
import nl.thehyve.ocdu.services.MappingService;
import nl.thehyve.ocdu.services.OcUserService;
import nl.thehyve.ocdu.services.UploadSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by piotrzakrzewski on 11/04/16.
 */
@Controller
@RequestMapping("/upload")
public class UploadController {


    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    FileService fileService;

    @Autowired
    OcUserService ocUserService;

    @Autowired
    UploadSessionService uploadSessionService;

    @Autowired
    MappingService mappingService;

    @RequestMapping(value = "/data", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> uploadFile(
            @RequestParam("uploadfile") MultipartFile uploadfile, HttpSession session) {

        try {
            OcUser user = ocUserService.getCurrentOcUser(session);
            Path locallySavedDataFile = saveFile(uploadfile);
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            fileService.depositDataFile(locallySavedDataFile, user, currentUploadSession);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }



    private Path saveFile(MultipartFile file) throws IOException {
        // Get the filename and build the local file path
        String filename = file.getOriginalFilename();
        String directory = System.getProperty("java.io.tmpdir");
        String filepath = Paths.get(directory, filename).toString();

        // Save the file locally
        BufferedOutputStream stream =
                new BufferedOutputStream(new FileOutputStream(new File(filepath)));
        stream.write(file.getBytes());
        stream.close();
        return Paths.get(filepath);
    }


    @RequestMapping(value = "/upload-mapping", method = RequestMethod.POST)
    public ResponseEntity<List<OcItemMapping>> acceptMapping(HttpSession session, @RequestBody List<OcItemMapping> mappings) {
        if (!isValid(mappings)) {
            log.error("Incorrect mapping JSON provided.");
            return new ResponseEntity<>(mappings, HttpStatus.BAD_REQUEST);
        }
        UploadSession submission = uploadSessionService.getCurrentUploadSession(session);
        mappingService.applyMapping(mappings, submission);
        return new ResponseEntity<>(mappings, HttpStatus.OK);
    }

    private boolean isValid(List<OcItemMapping> mappings) {
        List<OcItemMapping> faulty = mappings.stream().filter(ocItemMapping -> {
            if (ocItemMapping.getCrfName() == null ||
                    ocItemMapping.getStudy() == null ||
                    ocItemMapping.getCrfVersion() == null ||
                    ocItemMapping.getEventName() == null ||
                    ocItemMapping.getOcItemName() == null ||
                    ocItemMapping.getUsrItemName() == null) return true;
            else return false;
        }).collect(Collectors.toList());
        if (faulty.size() > 0) {
            return false;
        } else return true;
    }

}
