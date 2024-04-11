package ru.vglinskii.storemonitor.decommissionedreportsimulator.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.model.Commodity;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.model.DecommissionedReport;

public class DecommissionedReportGeneratorService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DecommissionedReportGeneratorService.class);

    public byte[] generateReportContent(
            DecommissionedReport reportEntity,
            List<Commodity> commodities
    ) {
        LOGGER.info("Generating decommissioned report for store {}", reportEntity.getStoreId());

        try (var workbook = new XSSFWorkbook();
             var outputStream = new ByteArrayOutputStream()
        ) {
            var sheet = workbook.createSheet("Списанный товар");

            var headerRow = sheet.createRow(0);
            var headerFont = workbook.createFont();
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setBold(true);
            var headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            var headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Товар");
            headerCell.setCellStyle(headerStyle);

            headerCell = headerRow.createCell(1);
            headerCell.setCellValue("Идентификатор");
            headerCell.setCellStyle(headerStyle);


            var cellFont = workbook.createFont();
            cellFont.setFontName("Arial");
            cellFont.setFontHeightInPoints((short) 14);
            var cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setFont(cellFont);

            for (int i = 1; i <= commodities.size(); i++) {
                var commodity = commodities.get(i - 1);
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue(commodity.getName());
                cell.setCellStyle(cellStyle);

                cell = row.createCell(1);
                cell.setCellValue(commodity.getId().toString());
                cell.setCellStyle(cellStyle);
            }


            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(outputStream);
            LOGGER.info("Generated decommissioned report for store {}", reportEntity.getStoreId());

            return outputStream.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Failed to generate decommissioned report for store {}", reportEntity.getStoreId(), e);

            throw new ReportGenerationException(e);
        }
    }
}
