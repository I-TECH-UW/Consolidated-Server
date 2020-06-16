import React from "react";

import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus, faTrash, faEdit } from "@fortawesome/free-solid-svg-icons";

class Servers extends React.Component {

	constructor(props) {
		super(props);
		this.state = {
			isLoading: true,
			servers: []
		};
	}


	async componentDidMount() {
		const response = await fetch('https://host.openelis.org:8443/server/', {
			credentials: 'include'
		});
		const body = await response.json();
		this.setState({ servers: body, isLoading: false });
	}

	async deleteServer(server) {
		if (!window.confirm(`You are about to delete '${server.name}'. Are you sure?`)) {
			return;
		}

		await fetch(`https://host.openelis.org:8443/server/${server.id}`, {
			method: 'DELETE',
			headers: {
				'Accept': 'application/json',
				'Content-Type': 'application/json'
			},
			credentials: 'include'
		}).then(() => {
			let updatedServers = [...this.state.servers].filter(i => i.id !== server.id);
			this.setState({ servers: updatedServers });
		});
	}

	render() {
		const { servers, isLoading } = this.state;

		if (isLoading) {
			return <p>Loading...</p>;
		}

		const dateFormat = 'en-US';
		const dateFormatOptions = {
			year: 'numeric',
			month: 'numeric',
			day: 'numeric',
			hour: 'numeric',
			minute: 'numeric'
		};
		const dateTimeFormat = new Intl.DateTimeFormat(dateFormat, dateFormatOptions);
		return (
			<div className="Servers container">
				<h2>Servers</h2>
				<div className="emptyServers" style={{ display: servers.length === 0 ? 'block' : 'none' }}>
					no servers exist <Link to={"/server/new"} className="btn"><FontAwesomeIcon icon={faPlus} /></Link>
				</div>
				<table style={{ display: servers.length > 0 ? 'block' : 'none' }}>
					<thead>
						<tr>
							<th>id</th>
							<th>name</th>
							<th>address</th>
							<th>last import success</th>
							<th><Link to={"/server/new"} className="btn"><FontAwesomeIcon icon={faPlus} /></Link></th>
						</tr>
					</thead>
					<tbody>
						{servers.map(server =>
							<tr key={server.id}>
								<td className="server-id" >{server.id}</td>
								<td className="server-name" ><Link to={"/server/" + server.id + "/view"} className="btn">{server.name}</Link></td>
								<td className="server-uri" >{server.uri}</td>
								<td className="server-lastImportSuccess" >
									<span className={server.lastImportSuccess === server.lastImportAttempt ? 'importSuccess' : 'importError'}>
										{dateTimeFormat.format(new Date(server.lastImportSuccess))}
									</span>
								</td>
								<td className="server-edit" ><Link to={"/server/" + server.id} className="btn"><FontAwesomeIcon icon={faEdit} /></Link></td>
								<td className="server-delete" ><button className="btn" onClick={() => this.deleteServer(server)}><FontAwesomeIcon icon={faTrash} /></button></td>
							</tr>
						)}
					</tbody>
				</table>

			</div>
		);
	}
}

export default Servers