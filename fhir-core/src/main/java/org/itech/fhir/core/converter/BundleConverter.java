package org.itech.fhir.core.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;

@Converter
public class BundleConverter implements AttributeConverter<Bundle, String> {

	private FhirContext fhirContext;

	public BundleConverter(FhirContext fhirContext) {
		this.fhirContext = fhirContext;
	}

	@Override
	public String convertToDatabaseColumn(Bundle attribute) {
		return fhirContext.newJsonParser().encodeResourceToString(attribute);
	}

	@Override
	public Bundle convertToEntityAttribute(String dbData) {
		return fhirContext.newJsonParser().parseResource(Bundle.class, dbData);
	}

}

