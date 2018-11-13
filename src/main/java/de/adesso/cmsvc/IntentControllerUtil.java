package de.adesso.cmsvc;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.config.SpringDataAnnotationBeanNameGenerator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Component
class IntentControllerUtil {

    private final static String INTENTS_SHEET_NAME = "Intents";
    private final static String INTENTS_ORIGINAL_SHEET_NAME = "Intents_Original";

    private final IntentsRepository intentsRepository;
    private final IntentsUtil intentsUtil;

    @Autowired
    IntentControllerUtil(IntentsRepository intentsRepository, IntentsUtil intentsUtil) {
        this.intentsRepository = intentsRepository;
        this.intentsUtil = intentsUtil;
    }

    void writeExcel(OutputStream outputStream) throws IOException {
        List<IntentEntity> intentEntities = intentsRepository.findFirstByOrderByIdDesc().entities;

        XSSFWorkbook workbook = new XSSFWorkbook();

        CellStyle lockedCellStyle = workbook.createCellStyle();
        lockedCellStyle.setLocked(true);

        XSSFSheet intentsSheet = workbook.createSheet(INTENTS_SHEET_NAME);
        XSSFSheet intentsOriginalSheet = workbook.createSheet(INTENTS_ORIGINAL_SHEET_NAME);
        int rowIndex = 0;
        for (IntentEntity intentEntity : intentEntities) {
            for (String example : intentEntity.examples) {
                XSSFRow row = intentsSheet.createRow(rowIndex);
                row.createCell(0, CellType.STRING).setCellValue(intentEntity.intent);
                row.createCell(1, CellType.STRING).setCellValue(example);

                row = intentsOriginalSheet.createRow(rowIndex++);
                XSSFCell cell = row.createCell(0, CellType.STRING);
                cell.setCellValue(intentEntity.intent);
                cell.setCellStyle(lockedCellStyle);

                cell = row.createCell(1, CellType.STRING);
                cell.setCellValue(example);
                cell.setCellStyle(lockedCellStyle);
            }
        }

        intentsSheet.autoSizeColumn(0);
        intentsSheet.autoSizeColumn(1);

        intentsOriginalSheet.autoSizeColumn(0);
        intentsOriginalSheet.autoSizeColumn(1);
        intentsOriginalSheet.protectSheet("123123");

        workbook.write(outputStream);
    }

    void readExcel(InputStream inputStream) throws IOException, InvalidFormatException {
        Map<String, IntentEntity> intentEntities = new HashMap<>();
        Map<String, IntentEntity> intentEntitiesOriginal = new HashMap<>();

        XSSFWorkbook workbook = new XSSFWorkbook(OPCPackage.open(inputStream));

        readIntentEntities(workbook.getSheet(INTENTS_SHEET_NAME), intentEntities);
        readIntentEntities(workbook.getSheet(INTENTS_ORIGINAL_SHEET_NAME), intentEntitiesOriginal);

        List<IntentChange> intentChanges = computeChanges(intentEntities, intentEntitiesOriginal);
        intentsUtil.applyChanges(intentChanges);
    }

    private List<IntentChange> computeChanges(Map<String, IntentEntity> intentEntities, Map<String, IntentEntity> intentEntitiesOriginal) {
        List<IntentChange> intentChanges = new ArrayList<>();

        Set<String> intents = new HashSet<>();
        intents.addAll(intentEntities.keySet());
        intents.addAll(intentEntitiesOriginal.keySet());

        for (String intent : intents) {
            final Set<String> examples;
            if (intentEntities.containsKey(intent)) {
                examples = new HashSet<>(intentEntities.get(intent).examples);
            } else {
                examples = new HashSet<>();
            }

            final Set<String> examplesOriginal;
            if (intentEntitiesOriginal.containsKey(intent)) {
                examplesOriginal = new HashSet<>(intentEntitiesOriginal.get(intent).examples);
            } else {
                examplesOriginal = new HashSet<>();
            }

            for (String example : examples) {
                if (!examplesOriginal.contains(example)) {
                    //Original does not contain example
                    intentChanges.add(new IntentChange(IntentChange.Type.Addition, intent, example));
                }
            }

            for (String original : examplesOriginal) {
                if (!examples.contains(original)) {
                    //Original was deleted
                    intentChanges.add(new IntentChange(IntentChange.Type.Removal, intent, original));
                }
            }
        }

        return intentChanges;
    }

    private void readIntentEntities(XSSFSheet sheet, Map<String, IntentEntity> intentEntities) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String intent = row.getCell(0).getStringCellValue();
            String example = row.getCell(1).getStringCellValue();
            intentEntities.computeIfAbsent(intent,
                    (__) -> new IntentEntity(intent, new ArrayList<>()))
                    .examples.add(example);
        }
    }
}
