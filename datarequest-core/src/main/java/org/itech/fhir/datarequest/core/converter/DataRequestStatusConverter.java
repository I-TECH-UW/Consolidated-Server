package org.itech.fhir.datarequest.core.converter;

import java.util.stream.Stream;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.itech.fhir.datarequest.core.model.DataRequestAttempt.DataRequestStatus;

@Converter(autoApply = true)
public class DataRequestStatusConverter implements AttributeConverter<DataRequestStatus, Character> {

	@Override
	public Character convertToDatabaseColumn(DataRequestStatus dataRequestStatus) {
		if (dataRequestStatus == null) {
			return null;
		}
		return dataRequestStatus.getCode();
	}

	@Override
	public DataRequestStatus convertToEntityAttribute(Character code) {
		if (code == null) {
			return null;
		}

		return Stream.of(DataRequestStatus.values())
				.filter(dataRequestStatus -> code.equals(dataRequestStatus.getCode()))
				.findFirst()
				.orElseThrow(IllegalArgumentException::new);
	}

}
