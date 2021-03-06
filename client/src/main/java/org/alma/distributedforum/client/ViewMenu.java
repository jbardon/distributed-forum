package org.alma.distributedforum.client;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.alma.distributedforum.server.IForumServer;
import org.alma.distributedforum.server.ISubject;
import org.alma.distributedforum.server.exception.SubjectAlreadyExist;
import org.alma.distributedforum.server.exception.SubjectNotFound;
import org.alma.distributedforum.server.exception.SubscribeListeningException;

public class ViewMenu {

    private ICustomerForum custumerForum;
    private IForumServer forumServer;
    private String host;
    private int port;
    private String lookup;
    private JComboBox<String> subjectComboB;
    private JFrame window;

    public ViewMenu(String host, int port, String lookup) {
        this.host = host;
        this.port = port;
        this.lookup = lookup;

        subjectComboB = new JComboBox<String>();

        try {
            custumerForum = new CustomerForum(this);
            connectServer();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void appendSubject(ISubject subject) {
        try {
            subjectComboB.addItem(subject.getName());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private synchronized void connectServer() {
        try {
            Registry reg = LocateRegistry.getRegistry(host, port);
            forumServer = (IForumServer) reg.lookup(lookup);

            subjectComboB.removeAllItems();
            List<ISubject> subjects = forumServer.listSubject(custumerForum);
            for (ISubject subject : subjects) {
                appendSubject(subject);
            }

        } catch (RemoteException | NotBoundException e) {
            JOptionPane.showMessageDialog(window,
                    "the server is down, sorry for the inconvenience!",
                    "Connexion Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void removeSubject(ISubject subject) {
        try {
            subjectComboB.removeItem(subject.getName());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void showMenu() throws RemoteException {

        window = new JFrame("Distributed-Forum");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(300, 250);
        window.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();

        gc.fill = GridBagConstraints.BOTH;

        /*
         * ipady permet de savoir où on place le composant s'il n'occupe pas la
         * totalité de l'espace disponnible
         */
        gc.ipady = gc.anchor = GridBagConstraints.CENTER;

        gc.weightx = 2;
        gc.weighty = 4;

        /* changing the fill */
        gc.ipady = GridBagConstraints.NONE;
        gc.fill = GridBagConstraints.NONE;

        /* layout constraint of the userName Label */
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 1;
        gc.gridheight = 1;

        JLabel nameLab = new JLabel("User name :");
        panel.add(nameLab, gc);

        /* layout constraint of the textFields */
        gc.gridx = 1;
        gc.gridy = 0;
        gc.gridwidth = 1;
        gc.gridheight = 1;

        final JTextField textEnter = new JTextField(10);
        /* adding the textField to the panel with layout constraint */
        panel.add(textEnter, gc);

        /* layout constraint of the Subscribing Label */
        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 1;
        gc.gridheight = 1;

        JLabel subscribingLabel = new JLabel("Subscribe to :");
        panel.add(subscribingLabel, gc);

        /* layout constraint of the Combo box */
        gc.gridx = 1;
        gc.gridy = 1;
        gc.gridwidth = 1;
        gc.gridheight = 1;

        /* adding the textField to the panel with layout constraint */
        panel.add(subjectComboB, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.gridwidth = 1;
        JLabel createSubLabel = new JLabel("Or create one : ");
        panel.add(createSubLabel, gc);

        gc.gridx = 1;
        gc.gridy = 2;
        gc.gridwidth = 1;
        JButton createSubBtn = new JButton("create subject");

        createSubBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!textEnter.getText().isEmpty()) {
                        JPanel diagPan = new JPanel(
                                new FlowLayout(FlowLayout.CENTER));
                        final JDialog createSubDial = new JDialog(window);
                        final JTextField newSubName = new JTextField(
                                "Enter subject name");

                        JButton createSub = new JButton("create");

                        createSub.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (!newSubName.getText().isEmpty()
                                        && !newSubName.getText()
                                                .equals("Enter subject name")) {
                                    try {
                                        createSubject(textEnter, createSubDial,
                                                newSubName);
                                    } catch (RemoteException e1) {
                                        connectServer();
                                        try {
                                            createSubject(textEnter,
                                                    createSubDial, newSubName);
                                        } catch (RemoteException
                                                | SubjectAlreadyExist e2) {
                                            e2.printStackTrace();
                                        }
                                    } catch (SubjectAlreadyExist e1) {
                                        errorSubjectExist(
                                                e1.getExistingSubject());
                                    }
                                }

                            }

                            private void createSubject(
                                    final JTextField textEnter,
                                    final JDialog createSubDial,
                                    final JTextField newSubName)
                                            throws RemoteException,
                                            SubjectAlreadyExist {
                                ISubject subjectObj = forumServer
                                        .createSubject(newSubName.getText());
                                ForumCustomer fc = new ForumCustomer(
                                        textEnter.getText());
                                ViewForum vf = new ViewForum(subjectObj, fc);
                                vf.showForum();

                                // window.setVisible(false);
                                createSubDial.setVisible(false);
                            }
                        });

                        diagPan.add(newSubName);
                        diagPan.add(createSub);

                        createSubDial.add(diagPan);
                        createSubDial.setSize(200, 100);
                        createSubDial.setVisible(true);
                    }
                } catch (Exception ignored) {
                }
            }
        });

        panel.add(createSubBtn, gc);

        /* layout constraint of the button */
        gc.gridx = 0;
        gc.gridy = 3;
        gc.gridwidth = 0;
        JButton sendBtn = new JButton("Connect to the discussion !");

        /* listener on the sendButton */
        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName = textEnter.getText();
                String subject = (String) subjectComboB.getSelectedItem();
                try {
                    if (!userName.isEmpty()) {
                        connectSubject(userName, subject);
                    }
                } catch (RemoteException e1) {
                    connectServer();
                    try {
                        connectSubject(userName, subject);
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
                    } catch (SubjectNotFound e2) {
                        errorSubjectNotFound(e2.getMessage());
                    }
                } catch (SubjectNotFound e1) {
                    errorSubjectNotFound(e1.getMessage());
                }
            }

            private void connectSubject(String userName, String subject)
                    throws RemoteException, SubjectNotFound {
                // window.setVisible(false);
                ISubject subjectObj = forumServer.getSubject(subject);
                ForumCustomer fc = new ForumCustomer(userName);
                ViewForum vf = new ViewForum(subjectObj, fc);
                vf.showForum();
            }
        });

        /* adding the button to the panel */
        panel.add(sendBtn, gc);

        /* layout constraint of the button */
        gc.gridx = 0;
        gc.gridy = 4;
        gc.gridwidth = 0;
        JButton removeSubjectBtn = new JButton("Remove the discussion !");

        /* listener on the sendButton */
        removeSubjectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nameSubject = (String) subjectComboB.getSelectedItem();
                try {
                    deleteSubject(nameSubject);
                } catch (RemoteException e1) {
                    connectServer();
                    try {
                        deleteSubject(nameSubject);
                    } catch (RemoteException re) {
                    } catch (SubscribeListeningException sle2) {
                        JOptionPane.showMessageDialog(window,
                                "Someone is still subscribed !", "Delete Error",
                                JOptionPane.ERROR_MESSAGE);

                    } catch (SubjectNotFound e2) {
                        errorSubjectNotFound(e2.getMessage());
                    }
                } catch (SubjectNotFound snf1) {
                    errorSubjectNotFound(snf1.getMessage());

                } catch (SubscribeListeningException sle1) {
                    JOptionPane.showMessageDialog(window,
                            "Someone is still subscribed !", "Delete Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            private void deleteSubject(String nameSubject)
                    throws RemoteException, SubscribeListeningException,
                    SubjectNotFound {
                forumServer.deleteSubject(nameSubject);
            }
        });

        /* adding the button to the panel */
        panel.add(removeSubjectBtn, gc);

        window.add(panel);
        window.setVisible(true);
    }

    private void errorSubjectNotFound(String message) {
        JOptionPane.showMessageDialog(window, message, "Subject Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void errorSubjectExist(ISubject existingSubject) {
        try {
            JOptionPane.showMessageDialog(window,
                    "Subject : " + existingSubject.getName() + " already exist",
                    "Subject Error", JOptionPane.ERROR_MESSAGE);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
