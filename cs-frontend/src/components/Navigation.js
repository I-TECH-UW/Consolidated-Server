import React from "react";
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
// import { faLanguage } from "@fortawesome/free-solid-svg-icons";
// import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { FormattedMessage } from 'react-intl';
import { Link, withRouter } from "react-router-dom";


class Navigation extends React.Component {

  render() {
    return (
      <div className="navigation">
        <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
          <div className="container">
            <Link className="navbar-brand" to="/">
              {/* {languages.title} */}
            </Link>
            <button
              className="navbar-toggler"
              type="button"
              data-toggle="collapse"
              data-target="#navbarResponsive"
              aria-controls="navbarResponsive"
              aria-expanded="false"
              aria-label="Toggle navigation"
            >
              <span className="navbar-toggler-icon"></span>
            </button>
            <div className="collapse navbar-collapse" id="navbarResponsive">
              <ul className="navbar-nav ml-auto">
                <li
                  className={`nav-item  ${
                    this.props.location.pathname === "/" ? "active" : ""
                    }`}
                >
                  <Link className="nav-link" to="/">
                    <FormattedMessage id="nav.item.home"
                      defaultMessage="Home"
                      description="Go to home page" />
                    <span className="sr-only">(current)</span>
                  </Link>
                </li>
                <li
                  className={`nav-item  ${
                    this.props.location.pathname === "/server" ? "active" : ""
                    }`}
                >
                  <Link className="nav-link" to="/server">
                    <FormattedMessage id="nav.item.server"
                      defaultMessage="Server"
                      description="Go to server page" />
                  </Link>
                </li>
                {/* TODO add this once we can edit resource groups through the app */}
                {/* <li
                  className={`nav-item  ${
                    this.props.location.pathname === "/resourceGroup" ? "active" : ""
                    }`}
                >
                  <Link className="nav-link" to="/resourceGroup">
                    <FormattedMessage id="nav.item.resourceGroup"
                      defaultMessage="Resource Group"
                      description="Go to resource group page" />
                  </Link>
                </li> */}

                {/* TODO add this back when/if we want to do internationalization */}
                {/* <li >
                  <div className="dropdown">
                    <button type="button" className="btn btn-dark" data-toggle="dropdown">
                      <FontAwesomeIcon icon={faLanguage} style={{ color: 'grey' }} size="2x" />
                    </button>
                    <div className="dropdown-menu">
                      <a className="dropdown-item" lang="en" onClick={this.props.onChangeLanguage}>English</a>
                      <a className="dropdown-item" lang="fr" onClick={this.props.onChangeLanguage}>Français</a>
                    </div>
                  </div>
                </li> */}
              </ul>
            </div>
          </div>
        </nav>
      </div>
    );
  }
}

export default withRouter(Navigation);