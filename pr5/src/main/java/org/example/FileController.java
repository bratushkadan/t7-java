package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
public class FileController {
    @Autowired
    private FileEntityRepository fileEntityRepository;
    @GetMapping("/")
    public String listUploadedFiles(Model model){
        List<FileEntity> files = fileEntityRepository.findAll();
        model.addAttribute("files", files);
        return "index";
    }
    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("filename") String filename) {
        if (!file.isEmpty()) {
            try {
                FileEntity fileEntity = new FileEntity();
                fileEntity.setFileName(filename);
                fileEntity.setFileData(file.getBytes());
                System.out.println(file.getBytes().toString());
                fileEntity.setFileSize(file.getSize());
                fileEntityRepository.save(fileEntity);
                return new ResponseEntity<>("File uploaded successfully!", HttpStatus.OK);
            } catch (IOException e) {
                System.err.println("File upload failed: " + e.getMessage());
                return new ResponseEntity<>("Error uploading file", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
    }
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> serveFile(@PathVariable("id") Long id) {
        Optional<FileEntity> fileEnt =
                fileEntityRepository.findById(id);
        if (!fileEnt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        FileEntity fileEntity = fileEnt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + "\"");

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(fileEntity.getFileData(), headers, HttpStatus.OK);
    }
    @GetMapping("/delete/{id}")
    public String deleteFile(@PathVariable("id") Long id) {
        fileEntityRepository.deleteById(id);
        return "redirect:/";
    }
}