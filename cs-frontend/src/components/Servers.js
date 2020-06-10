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
		const response = await fetch('http://localhost:8083/server/');
		const body = await response.json();
		this.setState({ servers: body, isLoading: false });
	}

	async deleteServer(id) {
    await fetch(`http://localhost:8083/server/${id}`, {
      method: 'DELETE',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    }).then(() => {
      let updatedServers = [...this.state.servers].filter(i => i.id !== id);
      this.setState({servers: updatedServers});
    });
  }

	render() {
		const { servers, isLoading } = this.state;

		if (isLoading) {
			return <p>Loading...</p>;
		}

		return (
			<div className="Servers container">
				<h2>Servers</h2>
				<div className="emptyServers" style={{display: servers.length === 0 ? 'block' : 'none'}}>
					no servers exist <Link to={"/server/new"} className="btn"><FontAwesomeIcon icon={faPlus} /></Link>
					</div>
				<table style={{display: servers.length > 0 ? 'block' : 'none'}}>
					<tr>
						<th>id</th>
						<th>name</th>
						<th>address</th>
						<th></th>
						<th></th>
						<th><Link to={"/server/new"} className="btn"><FontAwesomeIcon icon={faPlus} /></Link></th>
					</tr>
					{servers.map(server =>
						<tr key={server.id}>
							<td className="server-id" >{server.id}</td>
							<td className="server-name" ><Link to={"/server/" + server.id + "/view"} className="btn">{server.name}</Link></td>
							<td className="server-uri" >{server.uri}</td>
							<td className="server-edit" ><Link to={"/server/" + server.id} className="btn"><FontAwesomeIcon icon={faEdit} /></Link></td>
							<td className="server-delete" ><button className="btn" onClick={() => this.deleteServer(server.id)}><FontAwesomeIcon icon={faTrash} /></button></td>
						</tr>
					)}
				</table>
				
			</div>
		);
	}
}

export default Servers