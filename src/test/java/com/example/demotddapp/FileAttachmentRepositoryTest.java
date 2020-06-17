package com.example.demotddapp;

import com.example.demotddapp.model.FileAttachment;
import com.example.demotddapp.model.Hoax;
import com.example.demotddapp.repository.FileAttachmentRepository;
import com.example.demotddapp.utils.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class FileAttachmentRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Test
    public void test_findByDateBeforeAndHoaxIsNull_whenAttachmentsDateOlderThanOneHour_returnAll() {
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);

        assertThat(attachments.size()).isEqualTo(3);
    }

    @Test
    public void test_findByDateBeforeAndHoaxIsNull_whenAttachmentsDateOlderThanOneHourButHaveHoax_returnsNone() {
        Hoax hoax1 = testEntityManager.persist(TestUtils.createHoax());
        Hoax hoax2 = testEntityManager.persist(TestUtils.createHoax());
        Hoax hoax3 = testEntityManager.persist(TestUtils.createHoax());

        testEntityManager.persist(getOldFileAttachmentWithHoax(hoax1));
        testEntityManager.persist(getOldFileAttachmentWithHoax(hoax2));
        testEntityManager.persist(getOldFileAttachmentWithHoax(hoax3));

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);

        assertThat(attachments.size()).isEqualTo(0);
    }

    @Test
    public void test_findByDateBeforeAndHoaxIsNull_whenAttachmentsDateWithinOneHour_returnsNone() {
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);

        assertThat(attachments.size()).isEqualTo(0);
    }

    @Test
    public void test_findByDateBeforeAndHoaxIsNull_whenSomeAttachmentsOldSomeNewAndSomeWithHoax_returnsAttachmentsWithOlderAndNoHoaxAssign() {
        Hoax hoax1 = testEntityManager.persist(TestUtils.createHoax());

        testEntityManager.persist(getOldFileAttachmentWithHoax(hoax1));
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getFileAttachmentWithinOneHour());

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);

        assertThat(attachments.size()).isEqualTo(1);
    }

    private FileAttachment getOneHourOldFileAttachment() {
        Date date = new Date(System.currentTimeMillis() - (60 * 60 * 1000) - 1);
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }

    private FileAttachment getFileAttachmentWithinOneHour() {
        Date date = new Date(System.currentTimeMillis() - (60 * 1000));
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }

    private FileAttachment getOldFileAttachmentWithHoax(Hoax hoax) {
        FileAttachment fileAttachment = getOneHourOldFileAttachment();
        fileAttachment.setHoax(hoax);
        return fileAttachment;
    }
}
