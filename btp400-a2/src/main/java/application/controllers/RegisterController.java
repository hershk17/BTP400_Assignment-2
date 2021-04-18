package application.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/register")
public class RegisterController {

	@RequestMapping(method = RequestMethod.GET)
	public String register() {
		return "registration";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String handleRegister(Model model, @RequestParam String username, @RequestParam String password,
			@RequestParam String password2, RedirectAttributes redirAttrs) {

		if (password.equals(password2)) {

			String insert1 = "insert into walletusers(username, password, enabled) values(?,?,?);";
			String insert2 = "insert into user_roles(username, role) values(?,?);";

			try (Connection connection = DriverManager.getConnection(
					"jdbc:postgresql://ec2-54-209-43-223.compute-1.amazonaws.com:5432/d19rc88931g1bi", "luwrnpzmqrzvln",
					"9b76f4cfa5a87feb4cf28e8b90e485b183bca39b2b20c09e323b4a04b524b2ce");
					PreparedStatement insertUser = connection.prepareStatement(insert1);
					PreparedStatement insertUserRole = connection.prepareStatement(insert2)) {

				connection.setAutoCommit(false);

				BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
				String hashedPassword = passwordEncoder.encode(password);

				insertUser.setString(1, username);
				insertUser.setString(2, hashedPassword);
				insertUser.setBoolean(3, true);

				insertUserRole.setString(1, username);
				insertUserRole.setString(2, "USER");

				if (insertUser.executeUpdate() == 1 && insertUserRole.executeUpdate() == 1) {
					model.addAttribute("status", "Account created!");
					connection.commit();

				} else {
					model.addAttribute("status", "Unable to create user!");
					connection.rollback();
				}

			} catch (SQLException e1) {
				e1.printStackTrace();

			} catch (Exception e) {
				e.printStackTrace();

			}

		} else {
			model.addAttribute("status", "Passwords don't match!");
		}

		model.addAttribute("group", "Group 4");
		return "home";
	}

}