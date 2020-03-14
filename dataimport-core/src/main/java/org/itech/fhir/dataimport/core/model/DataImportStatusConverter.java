package org.itech.fhir.dataimport.core.model;

import java.util.stream.Stream;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.itech.fhir.dataimport.core.model.DataImportAttempt.DataImportStatus;

@Converter(autoApply = true)
public class DataImportStatusConverter implements AttributeConverter<DataImportStatus, Character> {

	@Override
	public Character convertToDatabaseColumn(DataImportStatus dataImportStatus) {
		if (dataImportStatus == null) {
			return null;
		}
		return dataImportStatus.getCode();
	}

	@Override
	public DataImportStatus convertToEntityAttribute(Character code) {
		if (code == null) {
			return null;
		}

		return Stream.of(DataImportStatus.values()).filter(dataImportStatus -> code.equals(dataImportStatus.getCode()))
				.findFirst()
				.orElseThrow(IllegalArgumentException::new);
	}

}
