var UserDetail = React.createClass({
  loadUsers: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  getInitialState: function() {
    return {data: []};
  },
  componentDidMount: function() {
    this.loadUsers();
    setInterval(this.loadUsers, this.props.pollInterval);
  },
  render: function() {
    return (
      <div className="">
        <h1>Users</h1>
        <UserForm />
        <Users data={this.state.data} />
      </div>
    );
  }
});

var Users = React.createClass({
  render: function() {
    var userNodes = this.props.data.map(function (user) {
      return (
        <User key={user.id} name={user.name} address={user.address} designation={user.designation} />
      );
    });

    return (
      <div className="well">
        {userNodes}
      </div>
    );
  }
});

var User = React.createClass({
  render: function() {
    return (
      <blockquote>
        <p>{this.props.name}</p>
        <strong>{this.props.address}</strong>
        <small>{this.props.designation}</small>
      </blockquote>
    );
  }
});

var UserForm = React.createClass({
  handleSubmit: function(e) {
    e.preventDefault();
        
    var formData = $("#userForm").serialize();

    var saveUrl = "http://localhost:9000/users/save";
    $.ajax({
      url: saveUrl,
      method: 'POST',
      dataType: 'json',
      data: formData,
      cache: false,
      success: function(data) {
        console.log(data)
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(saveUrl, status, err.toString());
      }.bind(this)
    });

    // clears the form fields
    React.findDOMNode(this.refs.name).value = '';
    React.findDOMNode(this.refs.address).value = '';
    React.findDOMNode(this.refs.designation).value = '';
    return;
  },
  render: function() {
    return (
    	<div className="row">
      		<form id="userForm" onSubmit={this.handleSubmit}>
		        <div className="col-xs-3">
		          <div className="form-group">
		            <input type="text" name="name" required="required" ref="name" placeholder="Name" className="form-control" />
		          </div>
		        </div>
		        <div className="col-xs-3">
		          <div className="form-group">
		            <input type="text" name="address"required="required"  ref="address" placeholder="Address" className="form-control" />
		          </div>
		        </div>
		        <div className="col-xs-3">
		          <div className="form-group">
		            <input type="text" name="designation" required="required" ref="designation" placeholder="Designation" className="form-control" />
		            <span className="input-icon fui-check-inverted"></span>
		          </div>
		        </div>
		        <div className="col-xs-3">
		          <input type="submit" className="btn btn-block btn-info" value="Add" />
		        </div>
			</form>
	   </div>
    );
  }
});

React.render(<UserDetail url="http://localhost:9000/users" pollInterval={2000} />, document.getElementById('content'));
