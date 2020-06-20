import React from "react";

import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus, faEdit } from "@fortawesome/free-solid-svg-icons";

class ResourceGroups extends React.Component {

	constructor(props) {
		super(props);
		this.state = {
			isLoading: true,
			resourceGroups: []
		};
	}


	async componentDidMount() {
		const response = await fetch(`${process.env.REACT_APP_DATA_IMPORT_API}/fhirResourceGroup/`, {
			credentials: 'include'
		});
		const body = await response.json();
		this.setState({ resourceGroups: body, isLoading: false });
	}

	render() {
		const { resourceGroups, isLoading } = this.state;

		if (isLoading) {
			return <p>Loading...</p>;
		}

		return (
			<div className="ResourceGroups container">
				<h2>Resource Groups</h2>
				<div className="emptyResourceGroup" style={{display: resourceGroups.length === 0 ? 'block' : 'none'}}>
					no resource groups exist <Link to={"/resourceGroups/new"} className="btn"><FontAwesomeIcon icon={faPlus} /></Link>
					</div>
				<table style={{display: resourceGroups.length > 0 ? 'block' : 'none'}}>
					<thead>
					<tr>
						<th>id</th>
						<th>name</th>
						<th></th>
						<th><Link to={"/resourceGroup/new"} className="btn"><FontAwesomeIcon icon={faPlus} /></Link></th>
					</tr>
					</thead>
					<tbody>
					{resourceGroups.map(resourceGroup =>
						<tr key={resourceGroup.id}>
							<td className="resourceGroup-id" >{resourceGroup.id}</td>
							<td className="resourceGroup-name" >{resourceGroup.resourceGroupName}</td>
							<td className="resourceGroup-edit" ><Link to={"/resourceGroup/" + resourceGroup.id} className="btn"><FontAwesomeIcon icon={faEdit} /></Link></td>
						</tr>
					)}
					</tbody>
				</table>
				
			</div>
		);
	}
}

export default ResourceGroups