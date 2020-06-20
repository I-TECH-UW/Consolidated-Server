import React from "react";

import { FormattedMessage } from 'react-intl';

class EditResourceGroup extends React.Component {

	constructor(props) {
		super(props);
		// this.lastNameChecked = '';
		this.state = {
			resourceGroup: {
				id: '',
				resourceGroupName: '',
				resourceSearchParams: {},
			},
			checked: {

			},
			errors: {
				resourceGroupName: '',
			},
			required: {
				resourceGroupName: true,
			},
			touched: {
				resourceGroupName: false,
			},
			formValid: false,
		};
		this.resourceTypes = [
			'Account', 'ActivityDefinition', 'AdverseEvent', 'AllergyIntolerance', 'Appointment', 'AppointmentResponse', 'AuditEvent', 'Basic', 'Binary', 'BiologicallyDerivedProduct', 'BodyStructure', 'Bundle', 'CapabilityStatement', 'CarePlan', 'CareTeam', 'CatalogEntry', 'ChargeItem', 'ChargeItemDefinition', 'Claim', 'ClaimResponse', 'ClinicalImpression', 'CodeSystem', 'Communication', 'CommunicationRequest', 'CompartmentDefinition', 'Composition', 'ConceptMap', 'Condition', 'Consent', 'Contract', 'Coverage', 'CoverageEligibilityRequest', 'CoverageEligibilityResponse', 'DetectedIssue', 'Device', 'DeviceDefinition', 'DeviceMetric', 'DeviceRequest', 'DeviceUseStatement', 'DiagnosticReport', 'DocumentManifest', 'DocumentReference', 'EffectEvidenceSynthesis', 'Encounter', 'Endpoint', 'EnrollmentRequest', 'EnrollmentResponse', 'EpisodeOfCare', 'EventDefinition', 'Evidence', 'EvidenceVariable', 'ExampleScenario', 'ExplanationOfBenefit', 'FamilyMemberHistory', 'Flag', 'Goal', 'GraphDefinition', 'Group', 'GuidanceResponse', 'HealthcareService', 'ImagingStudy', 'Immunization', 'ImmunizationEvaluation', 'ImmunizationRecommendation', 'ImplementationGuide', 'InsurancePlan', 'Invoice', 'Library', 'Linkage', 'List', 'Location', 'Measure', 'MeasureReport', 'Media', 'Medication', 'MedicationAdministration', 'MedicationDispense', 'MedicationKnowledge', 'MedicationRequest', 'MedicationStatement', 'MedicinalProduct', 'MedicinalProductAuthorization', 'MedicinalProductContraindication', 'MedicinalProductIndication', 'MedicinalProductIngredient', 'MedicinalProductInteraction', 'MedicinalProductManufactured', 'MedicinalProductPackaged', 'MedicinalProductPharmaceutical', 'MedicinalProductUndesirableEffect', 'MessageDefinition', 'MessageHeader', 'MolecularSequence', 'NamingSystem', 'NutritionOrder', 'Observation', 'ObservationDefinition', 'OperationDefinition', 'OperationOutcome', 'Organization', 'OrganizationAffiliation', 'Parameters', 'Patient', 'PaymentNotice', 'PaymentReconciliation', 'Person', 'PlanDefinition', 'Practitioner', 'PractitionerRole', 'Procedure', 'Provenance', 'Questionnaire', 'QuestionnaireResponse', 'RelatedPerson', 'RequestGroup', 'ResearchDefinition', 'ResearchElementDefinition', 'ResearchStudy', 'ResearchSubject', 'RiskAssessment', 'RiskEvidenceSynthesis', 'Schedule', 'SearchParameter', 'ServiceRequest', 'Slot', 'Specimen', 'SpecimenDefinition', 'StructureDefinition', 'StructureMap', 'Subscription', 'Substance', 'SubstancePolymer', 'SubstanceProtein', 'SubstanceReferenceInformation', 'SubstanceSpecification', 'SubstanceSourceMaterial', 'SupplyDelivery', 'SupplyRequest', 'Task', 'TerminologyCapabilities', 'TestReport', 'TestScript', 'ValueSet', 'VerificationResult', 'VisionPrescription']
	}

	async componentDidMount() {
		if (this.props.match.params.id !== 'new') {
			const resourceGroup = await (await fetch(`${process.env.REACT_APP_DATA_IMPORT_API}/fhirResourceGroup/${this.props.match.params.id}`), {
				credentials: 'include'
			}).json();
			const resourceSearchParams = await (await fetch(`${process.env.REACT_APP_DATA_IMPORT_API}/fhirResourceGroup/${this.props.match.params.id}/resourceSearchParams`), {
				credentials: 'include'
			}).json();
			const searchParams = {};
			for (var i = 0; i < resourceSearchParams.length; i++) {
				let resourceSearchParam = resourceSearchParams[i];
				if (resourceSearchParam.paramName) {
					searchParams[resourceSearchParam.resourceType] = { paramName: resourceSearchParam.paramName, paramValues: resourceSearchParam.paramValues };
				} else {
					searchParams[resourceSearchParam.resourceType] = {}
				}
			}
			this.setState({
				resourceGroup: {
					id: resourceGroup.id,
					resourceGroupName: resourceGroup.resourceGroupName,
					resourceSearchParams: searchParams,
				},
			});
			console.log(this.state);
		}
		Object.entries(this.state.required).forEach(
			([key, val]) => val && (this.state.resourceGroup[key].length === 0) && (this.state.errors[key] = 'error.field.required')
		);
	}


	handleChange = (e) => {
		e.preventDefault();
		const { name, value } = e.target;
		let errors = this.state.errors;
		let touched = this.state.touched;
		let required = this.state.required;
		let resourceGroup = this.state.resourceGroup;

		// block for additional validation logic
		switch (value) {
			case 'resourceGroupName':
				break;
			default:
				break;
		}
		touched[name] = true;
		required[name] && value.length < 1
			? errors[name] = 'error.field.required'
			: errors[name] = '';
		resourceGroup[name] = value;
		this.setState({ errors, touched, resourceGroup, 'formValid': this.validateForm(errors) }, () => {
			console.log(errors)
		})
	}

	validateForm = (errors) => {
		let valid = true;
		Object.values(errors).forEach(
			// if we have an error string set valid to false
			(val) => val.length > 0 && (valid = false)
		);
		return valid;
	}

	handleBlur = (e) => {
		e.preventDefault();
		// const { name, value } = e.target;
		const name = e.target.name;
		let touched = this.state.touched;
		touched[name] = true;
		// if (name === 'resourceGroupName') {
		// 	this.validateIfNameInUse(value);
		// }
	}

	// validateIfNameInUse = (resourceGroupName) => {
	// 	let alreadyChecked = resourceGroupName !== this.lastNameChecked;
	// 	let resourceGroupNameFilled = resourceGroupName.length > 0;
	// 	let newResourceGroup = this.state.resourceGroup.id.length === 0;
	// 	if ( alreadyChecked && resourceGroupNameFilled && newResourceGroup) {
	// 		fetch(`${process.env.REACT_APP_DATA_IMPORT_API}/resourceGroup/name/${resourceGroupName}/available`)
	// 			.then((response) => response.json())
	// 			.then((json) => { this.processResourceGroupNameCheck(resourceGroupName, json.response); });
	// 		this.lastNameChecked = resourceGroupName;
	// 	} else if (alreadyChecked && resourceGroupNameFilled) {
	// 		fetch(`${process.env.REACT_APP_DATA_IMPORT_API}/resourceGroup/${this.state.resourceGroup.id}/name/${resourceGroupName}/available`)
	// 			.then((response) => response.json())
	// 			.then((json) => { this.processResourceGroupNameCheck(resourceGroupName, json.response); });
	// 		this.lastNameChecked = resourceGroupName;
	// 	}
	// }
	//
	// processResourceGroupNameCheck = (resourceGroupName, response) => {
	// 	let errors = this.state.errors;
	// 	if (response === 'in use') {
	// 		console.log('"' + resourceGroupName + '" ' + response);
	// 		errors.resourceGroupName = 'error.field.resourceGroupName.notunique';
	// 	} else {
	// 		errors.resourceGroupName = '';
	// 	}
	// 	this.setState({ errors });
	// }

	handleSubmit = (e) => {
		e.preventDefault();
		var object = {};
		object.resourceGroupName = this.state.resourceGroup.resourceGroupName;
		object.resourceTypesSearchParams = this.state.resourceGroup.resourceSearchParams;
		var json = JSON.stringify(object);
		console.log(json);
		fetch(`${process.env.REACT_APP_DATA_IMPORT_API}/resourceGroup/`, {
			method: 'POST',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			body: json,
			credentials: 'include'
		}).then(this.props.history.push('/resourceGroup'));
	}

	handleCancel = () => {
		this.props.history.push('/resourceGroup')
	}

	shouldMarkError = (name) => {
		let hasError = this.state.errors[name].length > 0;
		let shouldShow = this.state.touched[name];

		return hasError ? shouldShow : false;
	}

	renderErrorMessage = (name) => {
		return (
			<FormattedMessage id={this.state.errors[name]}
				defaultMessage={this.state.errors[name]}
				description="Error field" />
		)
	}

	renderFhirResourceCheckbox = (value, label, index) => {
		return (
			<tr className="fhirResourceCheckbox" key={index}>
				<td>
					<input
						type="checkbox"
						onChange={() => this.handleCheck(value)} />
					<label htmlFor={label}> {label}</label>
					{this.state.checked[value] &&
						<div>
							<input type="text"
								// onChange={}
								value={this.state.resourceGroup.resourceSearchParams[value].paramName}
							/>
							=
						<input type="text"
								value={this.state.resourceGroup.resourceSearchParams[value].paramValues} />
						</div>}
				</td>
			</tr>
		);
	}

	handleCheck = (value) => {
		const checked = this.state.checked;
		const resourceSearchParams = this.state.resourceGroup.resourceSearchParams;
		checked[value] = !checked[value];
		if (checked[value]) {
			resourceSearchParams[value] = {};
		} else {
			delete resourceSearchParams[value];
		}
		this.setState({ checked: checked });
		console.log(this.state);
	}

	render() {
		const title = <h2>{this.state.resourceGroup.id ? 'Edit Resource Group' : 'Add Resource Group'}</h2>;

		return (
			<div className="EditResourceGroup container">
				{title}
				<form onSubmit={this.handleSubmit}>
					<div className="form-group row">
						<label>
							Resource Group Name:
						</label>
						<input type="text"
							className={this.shouldMarkError('resourceGroupName')
								? "form-control is-invalid"
								: "form-control"}
							value={this.state.resourceGroup.resourceGroupName}
							onChange={this.handleChange}
							onBlur={this.handleBlur} />
						<small className="text-danger" >
							{this.shouldMarkError('resourceGroupName') && this.renderErrorMessage('resourceGroupName')}
						</small>
					</div>
					<input type="submit" className="btn" value="Submit" disabled={!this.state.formValid} />
					<table>
						<tbody>
							{this.resourceTypes.map((resourceType, i) => this.renderFhirResourceCheckbox(resourceType, resourceType, i))}
						</tbody>
					</table>
					<button type="button" className="btn" onClick={this.handleCancel}>Cancel</button>
				</form>
			</div>
		);
	}
}

export default EditResourceGroup