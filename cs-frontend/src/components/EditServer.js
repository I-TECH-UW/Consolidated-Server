import React from "react";

import { FormattedMessage } from 'react-intl';

class EditServer extends React.Component {

	constructor(props) {
		super(props);
		this.lastNameChecked = '';
		this.state = {
			server: {
				id: '',
				serverName: '',
				serverUri: '',
			},
			errors: {
				serverName: '',
				serverUri: ''
			},
			required: {
				serverName: true,
				serverUri: true
			},
			touched: {
				serverName: false,
				serverUri: false,
			},
			formValid: false
		};
	}

	async componentDidMount() {
		if (this.props.match.params.id !== 'new') {
			let response = await fetch(`https://host.openelis.org:8443/server/${this.props.match.params.id}`, {
				credentials: 'include'
			})
			const server = await (response).json();
			response = await fetch(`https://host.openelis.org:8443/dataImportTask/server/${this.props.match.params.id}`, {
				credentials: 'include'
			});
			if (response.ok) {
				const dataImportTask = await (response).json();
				server.dataImportTask = dataImportTask;
			}
			this.setState({
				server: {
					id: server.id,
					serverName: server.name,
					serverUri: server.uri,
					dataImportTask: server.dataImportTask
				}
			});

		}
		Object.entries(this.state.required).forEach(
			([key, val]) => val && (this.state.server[key].length === 0) && (this.state.errors[key] = 'error.field.required')
		);
	}


	handleChange = (e) => {
		e.preventDefault();
		const { name, value } = e.target;
		let errors = this.state.errors;
		let touched = this.state.touched;
		let required = this.state.required;
		let server = this.state.server;

		// block for additional validation logic
		switch (value) {
			case 'serverName':
				break;
			case 'serverUri':
				break;
			default:
				break;
		}
		touched[name] = true;
		required[name] && value.length < 1
			? errors[name] = 'error.field.required'
			: errors[name] = '';
		server[name] = value;
		this.setState({ errors, touched, server, 'formValid': this.validateForm(errors) }, () => {
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
		const { name, value } = e.target;
		let touched = this.state.touched;
		touched[name] = true;
		if (name === 'serverName') {
			this.validateIfNameInUse(value);
		}
	}

	validateIfNameInUse = (serverName) => {
		let alreadyChecked = serverName !== this.lastNameChecked;
		let serverNameFilled = serverName.length > 0;
		let newServer = this.state.server.id.length === 0;
		if (alreadyChecked && serverNameFilled && newServer) {
			fetch(`https://host.openelis.org:8443/server/name/${serverName}/available`, {
				credentials: 'include'
			})
				.then((response) => response.json())
				.then((json) => { this.processServerNameCheck(serverName, json.response); });
			this.lastNameChecked = serverName;
		} else if (alreadyChecked && serverNameFilled) {
			fetch(`https://host.openelis.org:8443/server/${this.state.server.id}/name/${serverName}/available`, {
				credentials: 'include'
			})
				.then((response) => response.json())
				.then((json) => { this.processServerNameCheck(serverName, json.response); });
			this.lastNameChecked = serverName;
		}
	}

	processServerNameCheck = (serverName, response) => {
		let errors = this.state.errors;
		if (response === 'in use') {
			console.log('"' + serverName + '" ' + response);
			errors.serverName = 'error.field.serverName.notunique';
		} else {
			errors.serverName = '';
		}
		this.setState({ errors });
	}

	handleSubmit = (e) => {
		e.preventDefault();
		const formData = new FormData(e.target);
		console.log(formData);
		var object = {};
		formData.forEach((value, key) => { object[key] = value });
		var json = JSON.stringify(object);
		console.log(json);
		fetch('https://host.openelis.org:8443/server/', {
			method: 'POST',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			body: json,
			credentials: 'include'
		}).then(this.props.history.push('/server'));
	}

	handleCancel = () => {
		this.props.history.push('/server')
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

	render() {
		const title = <h2>{this.state.server.id ? 'Edit Server' : 'Add Server'}</h2>;

		return (
			<div className="EditServer container">
				{title}
				<form id="serverForm" onSubmit={this.handleSubmit}>
					<div className="form-group row">
						<label>
							Server Name:
						</label>
						<input type="text"
							className={this.shouldMarkError('serverName')
								? "form-control is-invalid"
								: "form-control"}
							name="serverName"
							value={this.state.server.serverName}
							onChange={this.handleChange}
							onBlur={this.handleBlur} />
						<small className="text-danger" >
							{this.shouldMarkError('serverName') && this.renderErrorMessage('serverName')}
						</small>
					</div>
					<div className="form-group row">
						<label>
							Server Address:
						</label>
						<input type="text"
							className={this.shouldMarkError('serverUri')
								? "form-control is-invalid"
								: "form-control"}
							name="serverUri"
							value={this.state.server.serverUri}
							onChange={this.handleChange}
							onBlur={this.handleBlur} />
						<small className="text-danger" >
							{this.shouldMarkError('serverUri') && this.renderErrorMessage('serverUri')}
						</small>
					</div>
					<input type="submit" className="btn" value="Submit" disabled={!this.state.formValid} />
					<button type="button" className="btn" onClick={this.handleCancel}>Cancel</button>
				</form>
				view
				<div>

				</div>
			</div>
		);
	}
}

export default EditServer